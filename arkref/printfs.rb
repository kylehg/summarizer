# for arkref.parsetuff.U

def cond code
  %|if (ARKref.showDebug()) {  #{code}  }|
end

for n in (1..20)
  args = (0..(n-1)).to_a
  types = args.map{|n| ["A"[0] + n].pack("c")}
  input = args.map{|n|   "#{types[n]} a#{n}"}.join(", ")
  passthru = args.map{|n| "a#{n}"}.join(", ")
  puts %|public static <#{types.join(",")}> void pf(String pat, #{input}) {  #{cond "System.out.printf(pat, #{passthru});"}  }|
  puts %|public static <#{types.join(",")}> String sf(String pat, #{input}) {  return String.format(pat, #{passthru});  }|
end
