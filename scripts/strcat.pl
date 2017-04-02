use warnings;
use strict;

my $file = "str.txt";
open(my $info, $file) or die "Couldn't open file";

while (my $line = <$info>) {
	chomp $line;
	print('oldCoteFieldNames.add("'.$line.'");'."\n");
}

close $info;
