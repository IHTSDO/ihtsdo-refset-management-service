#!/usr/bin/perl
while(<>) {
 chop; 
 $name = "$_"; 
 $_=<>; 
 chop; s/SCTID: //; 
 $id = "$_"; $name =~ s/(.*) \(.*\)/$1/; 
 $ns = "$id";
 $ns =~ s/.*(.......)10./$1/;
 $country = "UK" if $ns eq "1000000";
 $country = "AU" if $ns eq "1000036";
 $country = "US" if $ns eq "1000124";
 $i++;
 $folder = "refset$i";
 $ct=0;
 system "mkdir $folder";
 print "system mkdir $folder\n";
 open(IN,">$folder/der2_Refset_SimpleSnapshot_$country${ns}_20150131.txt") || die "nope\n";
 print IN "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\r\n";
 while (<>) {
     last if /----/;
     chop;
     s/.*\t(\d+)/$1/;
     next if length($_)>12;
     $uuid = `uuidgen`; chop($uuid);
     $module = "731000124108" if $country eq "US";
     $module = "999000051000000104" if $country eq "UK";
     $module = "32570231000036109" if $country eq "AU";
     print IN "$uuid\t20150731\t1\t$module\t$id\t$_\r\n";
     $ct++;
 }
 close(IN);
}
exit 0;
