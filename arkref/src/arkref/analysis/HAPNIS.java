package arkref.analysis;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import arkref.data.FirstNames;




/**
 * (deprecated and unused)
 * 
 * Implementation of Hal Daume's HAPNIS perl script for 
 * parsing the internal structure of names. 
 * (http://www.cs.utah.edu/~hal/HAPNIS/)
 * 
 * NB: this is not currently used in ARKref
 * 
 * @author Michael Heilman
 *
 */
public class HAPNIS {
	
	
	


	private List<String> inputParts;
	private List<String> labels;

	private static Set<String> firstNames;
	
	
	private static HAPNIS instance;
	
	private HAPNIS(){
		firstNames = FirstNames.getInstance().getAllFirstNames();
		inputParts = new ArrayList<String>();
		labels = new ArrayList<String>();
		
	}
	
	public static HAPNIS getInstance(){
		if(instance == null){
			instance = new HAPNIS();
		}
		return instance;
	}
	
	
	
	public void processPossibleName(String phrase) {
//	    chomp;
//	    my @names = split;
//	    my $N = scalar @names;
//	    my @select = ();
//	    for (my $i=0; $i<$N; $i++) { $select[$i] = 'Out'; }
//

		inputParts.clear();
		labels.clear();
		
		String part;
		String [] parts = phrase.split("\\s+");
		for(int i=0; i<parts.length; i++){
			inputParts.add(parts[i]);
			labels.add("other");
		}
		
		//System.err.println(inputParts.toString());
		
		int low = 0;
		int high = parts.length;
		
//	    # first, check for dr/mr/mrs/etc
//	    my $low = 0;
//	    if ($names[0] =~ /^[dm]rs?\.?$/i) {
//	        $select[0] = 'Role';
//	        $low = 1;
//	    }
		
		part = inputParts.get(0).toLowerCase();
		if(part.matches("^[dmf]rs?\\.?$")
				|| part.equals("professor")
				|| part.equals("president")
				|| part.equals("general")
				|| part.equals("captain"))
		{
			labels.set(0, "role");
			low = 1;
		}

//
//	    my $high = $N;
//	    if (($names[$high-1] eq 'I') ||
//	        ($names[$high-1] eq 'II') ||
//	        ($names[$high-1] eq 'III') ||
//	        ($names[$high-1] eq 'IV') ||
//	        ($names[$high-1] eq 'V') ||
//	        ($names[$high-1] eq 'esq') ||
//	        ($names[$high-1] eq 'esq.') ||
//	        ($names[$high-1] eq 'Esq') ||
//	        ($names[$high-1] eq 'Esq.') ||
//	        ($names[$high-1] eq 'Jr') ||
//	        ($names[$high-1] eq 'Jr.') ||
//	        ($names[$high-1] eq 'Sr') ||
//	        ($names[$high-1] eq 'Sr.')) {
//	        $select[$high-1] = 'Suffix';
//	        $high--;
//	    }
		
		part = inputParts.get(high-1);
		if(part.equals("I") 
			|| part.equals("II")
			|| part.equals("III")
			|| part.equals("IV")
			|| part.equals("V")
			|| part.equals("esq.")
			|| part.equals("esq")
			|| part.equals("Esq")
			|| part.equals("Esq.")
			|| part.equals("Jr")
			|| part.equals("Jr.")
			|| part.equals("Sr")
			|| part.equals("Sr."))
		{
			labels.set(high-1, "suffix");
			high--;
		}
		
//
//
//	    # now, go back to front
//	    if    ($high - $low == 1) { $select[$low] = 'Surname'; }
//	    elsif ($high - $low == 2) { $select[$low] = 'Forename'; $select[$low+1] = 'Surname'; }
//	    elsif ($high - $low >  2) {
//	        $select[$low] = 'Forename';
//	        $select[$high-1] = 'Surname';
//	        for (my $i=$low+1; $i<$high-1; $i++) { $select[$i] = 'Middle'; }
//	    }
//
		
		if(high-low == 1){
			labels.set(low, "surname");			
		}else if(high-low == 2){
			labels.set(low, "forename");
			labels.set(low+1, "surname");
		}else if(high-low > 2){
			labels.set(low, "forename");
			labels.set(high-1, "surname");
			for(int i=low+1; i<high-1; i++){
				labels.set(i, "middle");
			}
		}
	
		
//
//	    # now, check for links
//	    for (my $i=1; $i<$high; $i++) {
//	        if ($names[$i] =~ /-/) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^and$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^abu$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^de$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^du$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^del$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^\'$/i) { $select[$i] = 'Link'; }
//	        if ($names[$i] =~ /^o\'$/i) { $select[$i] = 'Surname'; }
//	    }
		
		for(int i=1; i<high; i++){
			part = inputParts.get(i).toLowerCase();
			boolean isLink = false;
			if(part.matches("-")){
				isLink = true;
			}else if(part.matches("^and$")){
				isLink = true;
			}else if(part.matches("^abu$")){
				isLink = true;
			}else if(part.matches("^de$")){
				isLink = true;
			}else if(part.matches("^du$")){
				isLink = true;
			}else if(part.matches("^del$")){
				isLink = true;
			}else if(part.matches("^\\'$")){
				isLink = true;
			}else if(part.matches("^o\\'$")){
				labels.set(i, "surname");
			}
			
			if(isLink){
				labels.set(i, "link");
			}
		}
		
		
//
//	    my $last = 'Surname';
//	    for (my $i=$high-1; $i>0; $i--) {
//	        if ($select[$i] eq 'Link') { $select[$i-1] = $last; }
//	        else { $last = $select[$i]; }
//	    }
		
		String lastLabel = "surname";
		for(int i=high-1; i>0; i--){
			if(labels.get(i).equals("link")){
				labels.set(i-1, lastLabel);
			}else{
				lastLabel = labels.get(i);
			}
		}
		
		
//
//	    # starting with middle is bad
//	    if ($select[$low] eq 'Middle') {
//	        $select[$low] = 'Forename';
//	        if (($low+2<$high) && ($select[$low+1] eq 'Link')) {
//	            $select[$low+2] = 'Forename';
//	        }
//	    }
		
		if(labels.get(low).equals("middle")){
			labels.set(low, "forename");
			if(low+2 < high && labels.get(low+1).equals("link")){
				labels.set(low+2, "forename");
			}
		}
		
		
//
//	    # check for first names only
//	    if (($low == 0) && ($N == 1) && (exists $firstNames{lc($names[0])})) {
//	        $select[0] = 'Forename';
//	    }
		
		if((low == 0) && (inputParts.size()==1) 
				&& firstNames.contains(inputParts.get(0).toLowerCase()))
		{
			labels.set(0, "forename");
		}
		
		
//
//	    # print output
//	    for (my $i=0; $i<$N; $i++) {
//	        if ($i > 0) { print ' '; }
//	        print $names[$i] . '_' . $select[$i];
//	    }
//	    print "\n";
		
		
	}
	
	
	private String getPartsForType(String type){
		String res = "";
		for(int i=0; i<labels.size(); i++){
			if(labels.get(i).equals(type)){
				if(res.length()>0){
					res+=" ";
				}
				res += inputParts.get(i);
			}
		}
		return res;
	}
	
	public String getRole() {
		return getPartsForType("role");
	}


	public String getSuffix() {
		return getPartsForType("suffix");
	}


	public String getForename() {
		return getPartsForType("forename");
	}


	public String getSurname() {
		return getPartsForType("surname");
	}


	public String getLink() {
		return getPartsForType("link");
	}

	public String getMiddle() {
		return getPartsForType("middle");
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HAPNIS h = HAPNIS.getInstance();
		
		System.err.println("enter a name to parse");
		String buf;
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while((buf = br.readLine()) != null){
			buf = buf.trim();
			h.processPossibleName(buf);
			System.out.println("Role:\t"+h.getRole());
			System.out.println("Forename:\t"+h.getForename());
			System.out.println("Link:\t"+h.getLink());
			System.out.println("Middle:\t"+h.getMiddle());
			System.out.println("Surname:\t"+h.getSurname());
			System.out.println("Suffix:\t"+h.getSuffix());
			
		}

	}
	
	
}


/*
 

 
 #!/usr/bin/perl -w
use strict;

my %firstNames = ();
while (1) {
    my $tmp = shift or last;
    if ($tmp eq '-names') { makeFirstNames(); }
    else { die "usage: hapnis.pl [-names] < file\n"; }
}


while (<>) {
    chomp;
    my @names = split;
    my $N = scalar @names;
    my @select = ();
    for (my $i=0; $i<$N; $i++) { $select[$i] = 'Out'; }

    # first, check for dr/mr/mrs/etc
    my $low = 0;
    if ($names[0] =~ /^[dm]rs?\.?$/i) {
        $select[0] = 'Role';
        $low = 1;
    }

    my $high = $N;
    if (($names[$high-1] eq 'I') ||
        ($names[$high-1] eq 'II') ||
        ($names[$high-1] eq 'III') ||
        ($names[$high-1] eq 'IV') ||
        ($names[$high-1] eq 'V') ||
        ($names[$high-1] eq 'esq') ||
        ($names[$high-1] eq 'esq.') ||
        ($names[$high-1] eq 'Esq') ||
        ($names[$high-1] eq 'Esq.') ||
        ($names[$high-1] eq 'Jr') ||
        ($names[$high-1] eq 'Jr.') ||
        ($names[$high-1] eq 'Sr') ||
        ($names[$high-1] eq 'Sr.')) {
        $select[$high-1] = 'Suffix';
        $high--;
    }


    # now, go back to front
    if    ($high - $low == 1) { $select[$low] = 'Surname'; }
    elsif ($high - $low == 2) { $select[$low] = 'Forename'; $select[$low+1] = 'Surname'; }
    elsif ($high - $low >  2) {
        $select[$low] = 'Forename';
        $select[$high-1] = 'Surname';
        for (my $i=$low+1; $i<$high-1; $i++) { $select[$i] = 'Middle'; }
    }


    # now, check for links
    for (my $i=1; $i<$high; $i++) {
        if ($names[$i] =~ /-/) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^and$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^abu$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^de$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^du$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^del$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^\'$/i) { $select[$i] = 'Link'; }
        if ($names[$i] =~ /^o\'$/i) { $select[$i] = 'Surname'; }
    }

    my $last = 'Surname';
    for (my $i=$high-1; $i>0; $i--) {
        if ($select[$i] eq 'Link') { $select[$i-1] = $last; }
        else { $last = $select[$i]; }
    }

    # starting with middle is bad
    if ($select[$low] eq 'Middle') {
        $select[$low] = 'Forename';
        if (($low+2<$high) && ($select[$low+1] eq 'Link')) {
            $select[$low+2] = 'Forename';
        }
    }

    # check for first names only
    if (($low == 0) && ($N == 1) && (exists $firstNames{lc($names[0])})) {
        $select[0] = 'Forename';
    }

    # print output
    for (my $i=0; $i<$N; $i++) {
        if ($i > 0) { print ' '; }
        print $names[$i] . '_' . $select[$i];
    }
    print "\n";
}




sub makeFirstNames {
    foreach my $n ('abby', 'abigail', 'ada', 'addie', 'adela', 'adele', 'adeline', 'adolfo', 'adriana', 'adrienne', 'agnes', 'aida', 'aileen', 'aimee', 'aisha', 'al', 'alan', 'alana', 'alberta', 'aldo', 'alec', 'alejandra', 'alexandra', 'alexandria', 'alfonzo', 'alfreda', 'alfredo', 'alice', 'alicia', 'aline', 'alisa', 'alisha', 'alison', 'alissa', 'allyson', 'alma', 'alphonse', 'alphonso', 'alta', 'althea', 'alvaro', 'alvin', 'alyce', 'alyson', 'alyssa', 'amalia', 'amanda', 'amber', 'amelia', 'amie', 'amparo', 'amy', 'ana', 'anastasia', 'andrea', 'andrew', 'andy', 'angela', 'angelia', 'angelica', 'angelina', 'angeline', 'angelique', 'angelita', 'angie', 'anita', 'ann', 'anna', 'annabelle', 'anne', 'annette', 'annie', 'annmarie', 'antoinette', 'antonia', 'antony', 'antwan', 'april', 'araceli', 'ariel', 'arlene', 'armando', 'arnulfo', 'arron', 'art', 'arturo', 'ashlee', 'audra', 'audrey', 'augusta', 'aurelia', 'aurelio', 'aurora', 'autumn', 'ava', 'barbara', 'barbra', 'beatrice', 'beatriz', 'beau', 'becky', 'belinda', 'ben', 'benita', 'bennie', 'bernadette', 'bernadine', 'bernice', 'bernie', 'bert', 'bertha', 'bertie', 'beryl', 'bessie', 'beth', 'bethany', 'betsy', 'bette', 'bettie', 'betty', 'bettye', 'beulah', 'beverley', 'bianca', 'billy', 'blanca', 'bobbi', 'bobbie', 'bobby', 'bonita', 'bonnie', 'brad', 'branden', 'brandi', 'brandie', 'brenda', 'brendan', 'bret', 'brian', 'briana', 'brianna', 'bridget', 'bridgett', 'bridgette', 'brigitte', 'britney', 'brittany', 'brittney', 'bryon', 'buddy', 'caitlin', 'caleb', 'callie', 'camille', 'candace', 'candice', 'candy', 'cara', 'carissa', 'carla', 'carlene', 'carly', 'carmela', 'carmella', 'carmelo', 'carmen', 'carmine', 'carol', 'carole', 'caroline', 'carolyn', 'carrie', 'casandra', 'cassandra', 'cassie', 'catalina', 'catherine', 'cathleen', 'cathryn', 'cathy', 'cecelia', 'cecile', 'cecilia', 'cedric', 'celeste', 'celia', 'celina', 'chad', 'chandra', 'charlene', 'charlotte', 'charmaine', 'chasity', 'chelsea', 'cheri', 'cherie', 'cheryl', 'chris', 'christa', 'christi', 'christina', 'christine', 'christoper', 'christopher', 'chrystal', 'chuck', 'cindy', 'clara', 'clarence', 'clarice', 'clarissa', 'claudette', 'claudia', 'claudine', 'cleo', 'clint', 'coleen', 'colette', 'colleen', 'concetta', 'connie', 'consuelo', 'corina', 'corine', 'corinne', 'cornelia', 'corrine', 'cristina', 'crystal', 'curt', 'cynthia', 'cyril', 'daisy', 'damien', 'dan', 'danial', 'danielle', 'danny', 'dante', 'daphne', 'daren', 'darin', 'darius', 'darla', 'darlene', 'darrel', 'darrell', 'darren', 'darrin', 'darryl', 'daryl', 'david', 'dawn', 'deana', 'deandre', 'deann', 'deanna', 'deanne', 'debbie', 'debora', 'deborah', 'debra', 'deena', 'deidre', 'deirdre', 'delbert', 'della', 'delmar', 'delores', 'deloris', 'demetrius', 'dena', 'denise', 'denver', 'deon', 'derek', 'derick', 'desiree', 'devin', 'devon', 'dewayne', 'diana', 'diane', 'diann', 'dianna', 'dianne', 'dina', 'dino', 'dirk', 'dixie', 'dollie', 'dolores', 'dominic', 'don', 'dona', 'donald', 'donn', 'donna', 'donnie', 'donny', 'dora', 'doreen', 'dorian', 'doris', 'dorothea', 'dorothy', 'dorthy', 'doug', 'duane', 'dustin', 'dusty', 'dwayne', 'dylan', 'earlene', 'earline', 'earnestine', 'ebony', 'ed', 'eddie', 'edgardo', 'edith', 'edmund', 'edna', 'eduardo', 'edward', 'edwardo', 'edwin', 'edwina', 'effie', 'efrain', 'efren', 'eileen', 'elaine', 'elba', 'eldon', 'eleanor', 'elena', 'eli', 'elijah', 'elinor', 'elisa', 'elisabeth', 'elise', 'eliseo', 'eliza', 'elizabeth', 'ella', 'ellen', 'elma', 'elmo', 'elnora', 'eloise', 'eloy', 'elsa', 'elsie', 'elva', 'elvia', 'elvin', 'elvira', 'elvis', 'emil', 'emile', 'emilia', 'emilio', 'emily', 'emma', 'enid', 'enrique', 'eric', 'erica', 'erich', 'erick', 'ericka', 'erik', 'erika', 'erin', 'erma', 'erna', 'ernest', 'ernestine', 'ernesto', 'ernie', 'errol', 'esmeralda', 'esperanza', 'essie', 'estela', 'estella', 'esther', 'ethan', 'ethel', 'etta', 'eugene', 'eugenia', 'eugenio', 'eula', 'eunice', 'eva', 'evangelina', 'evangeline', 'eve', 'evelyn', 'ezra', 'fannie', 'fanny', 'faustino', 'faye', 'felecia', 'felicia', 'fidel', 'florine', 'flossie', 'fran', 'frances', 'francesca', 'francine', 'francisca', 'frankie', 'fred', 'freddie', 'freddy', 'frederic', 'freida', 'frieda', 'gabriela', 'gabrielle', 'gail', 'galen', 'gena', 'genaro', 'gene', 'geneva', 'genevieve', 'geoffrey', 'george', 'georgette', 'georgia', 'georgina', 'gerald', 'geraldine', 'gerardo', 'gertrude', 'gilberto', 'gilda', 'gina', 'ginger', 'gino', 'giovanni', 'gladys', 'glenda', 'glenna', 'gloria', 'goldie', 'gonzalo', 'gracie', 'graciela', 'greg', 'greta', 'gretchen', 'gus', 'gustavo', 'gwen', 'gwendolyn', 'hal', 'hallie', 'harold', 'harriet', 'harriett', 'hattie', 'heather', 'heidi', 'helen', 'helena', 'helene', 'helga', 'henrietta', 'heriberto', 'herminia', 'herschel', 'hershel', 'hilary', 'hilda', 'hillary', 'hiram', 'humberto', 'ian', 'ida', 'ila', 'ilene', 'imelda', 'imogene', 'ina', 'ines', 'inez', 'ingrid', 'ira', 'irene', 'iris', 'irma', 'isabella', 'isaiah', 'isiah', 'isidro', 'ismael', 'iva', 'ivan', 'jackie', 'jacklyn', 'jaclyn', 'jacqueline', 'jacquelyn', 'jake', 'jamaal', 'jamal', 'jame', 'jamel', 'jami', 'jamie', 'jana', 'jane', 'janell', 'janet', 'janette', 'janice', 'janie', 'janine', 'janna', 'jannie', 'jarred', 'jarrod', 'jasmine', 'jason', 'jayson', 'jeanette', 'jeanie', 'jeanine', 'jeanne', 'jeannette', 'jeannie', 'jeannine', 'jeff', 'jeffrey', 'jeffry', 'jenifer', 'jenna', 'jennie', 'jennifer', 'jerald', 'jeremy', 'jeri', 'jermaine', 'jerold', 'jerri', 'jerrod', 'jerry', 'jesse', 'jessica', 'jesus', 'jewel', 'jill', 'jillian', 'jim', 'jimmie', 'jimmy', 'joan', 'joann', 'joanna', 'joanne', 'joaquin', 'jocelyn', 'jodi', 'jodie', 'jody', 'joe', 'joel', 'joesph', 'joey', 'johanna', 'john', 'johnathan', 'johnathon', 'johnie', 'johnnie', 'johnny', 'jolene', 'jon', 'jonathan', 'jonathon', 'joni', 'jose', 'josef', 'josefa', 'josefina', 'joseph', 'josephine', 'josh', 'joshua', 'josiah', 'josie', 'josue', 'juan', 'juana', 'juanita', 'judith', 'judy', 'julia', 'juliana', 'julianne', 'julie', 'juliet', 'juliette', 'julio', 'june', 'justin', 'justine', 'kaitlin', 'kara', 'kareem', 'karen', 'kari', 'karin', 'karina', 'karla', 'karyn', 'kasey', 'kate', 'katelyn', 'katharine', 'katherine', 'katheryn', 'kathie', 'kathleen', 'kathrine', 'kathryn', 'kathy', 'katie', 'katina', 'katrina', 'katy', 'kayla', 'keisha', 'kelli', 'kellie', 'kelvin', 'ken', 'kendra', 'kenneth', 'kennith', 'kenya', 'keri', 'kermit', 'kerri', 'kevin', 'kieth', 'kimberley', 'kimberly', 'kirsten', 'kitty', 'kris', 'krista', 'kristen', 'kristi', 'kristie', 'kristin', 'kristina', 'kristine', 'kristopher', 'kristy', 'krystal', 'kurtis', 'ladonna', 'lakeisha', 'lakisha', 'lana', 'lanny', 'larry', 'latasha', 'latisha', 'latonya', 'latoya', 'laura', 'lauren', 'lauri', 'laurie', 'lavern', 'laverne', 'lavonne', 'lawanda', 'leah', 'leann', 'leanna', 'leanne', 'leila', 'lela', 'lelia', 'lemuel', 'lena', 'lenora', 'lenore', 'leola', 'leona', 'leonel', 'leonor', 'leopoldo', 'les', 'lesa', 'lessie', 'leta', 'letha', 'leticia', 'letitia', 'lidia', 'lila', 'lilia', 'lilian', 'liliana', 'lillian', 'lily', 'lina', 'linda', 'linwood', 'lionel', 'lisa', 'liz', 'liza', 'lizzie', 'lois', 'lola', 'lolita', 'lon', 'lonnie', 'loraine', 'loren', 'lorena', 'lorene', 'loretta', 'lori', 'lorie', 'lorna', 'lorraine', 'lorrie', 'lottie', 'lou', 'louella', 'louisa', 'louise', 'lourdes', 'luann', 'lucile', 'lucille', 'lucinda', 'lucy', 'luella', 'luisa', 'lula', 'lupe', 'lydia', 'lynda', 'lynette', 'lynne', 'lynnette', 'mabel', 'mable', 'madeleine', 'madeline', 'madelyn', 'madge', 'magdalena', 'maggie', 'malinda', 'mamie', 'mandy', 'manuela', 'marc', 'marcel', 'marcelino', 'marcella', 'marci', 'marcia', 'marcie', 'margaret', 'margarita', 'margarito', 'margery', 'margie', 'margo', 'margret', 'marguerite', 'mari', 'maria', 'marian', 'mariana', 'marianne', 'maribel', 'maricela', 'marie', 'marietta', 'marilyn', 'marina', 'mario', 'marisa', 'marisol', 'marissa', 'maritza', 'marjorie', 'mark', 'marla', 'marlene', 'marlon', 'marquita', 'marsha', 'marta', 'martha', 'marva', 'mary', 'maryann', 'maryanne', 'maryellen', 'marylou', 'matilda', 'matthew', 'maude', 'maura', 'maureen', 'mavis', 'maxine', 'mayra', 'meagan', 'megan', 'meghan', 'melanie', 'melba', 'melinda', 'melisa', 'melissa', 'melody', 'melva', 'mervin', 'mia', 'micah', 'michael', 'micheal', 'michele', 'michelle', 'mike', 'milagros', 'mildred', 'millicent', 'millie', 'mindy', 'minerva', 'minnie', 'miriam', 'misty', 'mitch', 'mitzi', 'moises', 'mollie', 'molly', 'mona', 'monica', 'monique', 'muriel', 'myra', 'myrna', 'myrtle', 'nadia', 'nadine', 'nancy', 'nanette', 'nannie', 'naomi', 'natalia', 'natalie', 'natasha', 'nelda', 'nellie', 'nettie', 'neva', 'nichole', 'nickolas', 'nicole', 'nigel', 'nikki', 'nina', 'nita', 'noelle', 'noemi', 'nola', 'nona', 'nora', 'norbert', 'norberto', 'noreen', 'norma', 'octavio', 'odessa', 'ofelia', 'ola', 'olga', 'olivia', 'opal', 'ophelia', 'ora', 'orville', 'oscar', 'osvaldo', 'pamela', 'pansy', 'pat', 'patrica', 'patrice', 'patricia', 'patsy', 'paula', 'paulette', 'pauline', 'pearlie', 'peggy', 'penelope', 'peter', 'petra', 'phil', 'phillip', 'phoebe', 'phyllis', 'priscilla', 'quentin', 'rachael', 'rachel', 'rachelle', 'ramiro', 'ramona', 'randal', 'randi', 'randy', 'raquel', 'raul', 'raymundo', 'reba', 'rebecca', 'rebekah', 'refugio', 'reggie', 'regina', 'reginald', 'reinaldo', 'rena', 'renee', 'reuben', 'reva', 'reynaldo', 'rhoda', 'rhonda', 'richard', 'rickie', 'ricky', 'rigoberto', 'rita', 'rob', 'robbie', 'robby', 'robert', 'roberta', 'robin', 'robyn', 'rocky', 'rod', 'rodney', 'rodolfo', 'rodrick', 'rodrigo', 'rogelio', 'roger', 'rolando', 'ron', 'ronald', 'ronda', 'ronnie', 'ronny', 'rory', 'rosalie', 'rosalind', 'rosalinda', 'rosalyn', 'rosanna', 'rosanne', 'roseann', 'rosella', 'rosemarie', 'rosemary', 'rosendo', 'rosetta', 'rosie', 'roslyn', 'rowena', 'roxanne', 'roxie', 'rusty', 'ruthie', 'sabrina', 'sadie', 'sallie', 'sally', 'samantha', 'sammie', 'sammy', 'sandra', 'sang', 'sara', 'sarah', 'sasha', 'saundra', 'savannah', 'scot', 'scottie', 'scotty', 'sean', 'selena', 'selma', 'serena', 'sergio', 'shana', 'shanna', 'shari', 'sharlene', 'sharon', 'sharron', 'shaun', 'shauna', 'shawn', 'shawna', 'sheena', 'sheila', 'shelia', 'sheree', 'sheri', 'sherri', 'sherrie', 'sheryl', 'socorro', 'sofia', 'sondra', 'sonia', 'sonja', 'sonny', 'sonya', 'sophia', 'sophie', 'staci', 'stacie', 'stan', 'stefan', 'stefanie', 'stephanie', 'stephen', 'steve', 'steven', 'stevie', 'sue', 'susan', 'susana', 'susanna', 'susanne', 'susie', 'suzanne', 'suzette', 'sybil', 'sydney', 'tabatha', 'tabitha', 'tamara', 'tameka', 'tamera', 'tami', 'tamika', 'tammi', 'tammie', 'tammy', 'tamra', 'tania', 'tanisha', 'tanya', 'tara', 'tasha', 'ted', 'teddy', 'terence', 'teresa', 'teri', 'terra', 'terrance', 'terrence', 'terri', 'terrie', 'tessa', 'thad', 'thaddeus', 'thelma', 'theodore', 'theresa', 'therese', 'theron', 'tia', 'tim', 'timmy', 'timothy', 'tina', 'tisha', 'toby', 'tommie', 'tommy', 'toni', 'tonia', 'tony', 'tonya', 'traci', 'tracie', 'trenton', 'trevor', 'trey', 'tricia', 'trina', 'trisha', 'trudy', 'twila', 'ty', 'tyrone', 'ulysses', 'ursula', 'valarie', 'valeria', 'valerie', 'vanessa', 'velma', 'vern', 'verna', 'veronica', 'vicki', 'vickie', 'vicky', 'vilma', 'violet', 'virgie', 'virginia', 'vivian', 'vonda', 'wanda', 'wendi', 'wendy', 'wilda', 'wilfred', 'wilfredo', 'willa', 'william', 'willie', 'wilma', 'winfred', 'winifred', 'wm', 'yesenia', 'yolanda', 'yvette', 'yvonne', 'zelma') {
        $firstNames{$n} = 1;
    }
}

 
  
 */

