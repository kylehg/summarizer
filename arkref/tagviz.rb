#!/usr/bin/env ruby

# This takes the pseudo-xml mention-tagged format and converts it to a minimal
# text format similar to the one UIUC LBJ outputs by default.
#
# or, html.

# This depends on certain conventions in naming schemes for entity ids.
# So not especially stable.

require 'rubygems'
require 'hpricot'

hp = Hpricot STDIN.read

$html = ARGV.member?("-html")

$colors = %w[ maroon navy green orange purple magenta teal  ]
$color_i = -1

if ARGV.member?("-style")
  puts %|
<style>
  .singleton > .text { color:black }
  .singleton > .bracket { color: grey }
  .entityid { vertical-align: sub; font-size: 70%; }
</style>
  |
end

$short2long = {}
$long2color = {}

def short_eid long_eid
  short = long_eid.gsub(/_.*/,"")
  if ($short2long[short] || long_eid) != long_eid
    raise "bug with entity id format conventions"
  end
  $short2long[short] = long_eid
  short
end

def advance_color
  $color_i = ($color_i+1) % $colors.size
end
  
def entity_color(node)
  e = node['entityid']
  $long2color[e] ||= $colors[advance_color]
  $long2color[e]
end

def is_singleton(node)
  node['entityid'] !~ /_/
end

if !$html

  def start_mention(node)
    print "*"
  end
  def end_mention(node)
    if is_singleton(node)
      print "*"
    else
      print "*_#{short_eid node['entityid']}"
    end
  end
  def print_text(node)
    print node
  end

else

  def start_mention(node)
    if is_singleton(node)
      print "<span class=singleton>"
      print "<span class=bracket>[</span>"
    else
      print %|<span class=non_singleton style="color: #{entity_color node}">|
      print %|<span class=bracket>[</span>|
    end
  end
  def end_mention(node)
    if is_singleton(node)
      print %|<span class=bracket>]</span>|
      print %|</span>|
    else
      print %|<span class=bracket>]</span>|
      print %|<span class=entityid>#{short_eid node['entityid']}</span>|
      print %|</span>|
    end
  end
  def print_text(node)
    print "<span class=text>"
    print node.to_s.gsub("&","&amp;").gsub("<","&lt;").gsub(">","&gt;").gsub("\n", "<br>")
    print "</span>"
  end

end

####  Tree walk  ####

def process(node)
  if node.is_a? Hpricot::Text
    print_text(node)
  elsif node.is_a? Hpricot::Elem
    node.name=='mention' or raise "unknown node #{c.inspect}"
    start_mention(node)
    for child in node.children
      process(child)
    end
    end_mention(node)
  end
end
    
for c in hp.children
  process(c)
end
puts
 
