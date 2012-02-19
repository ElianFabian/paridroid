#!/bin/bash
PARI=./pari-dev
PROTO_DIR=./proto
XML_DIR=./proto/xml

mkdir -p $PROTO_DIR
mkdir -p $XML_DIR

subdirs=( conversions elliptic_curves linear_algebra number_fields operators programming symbolic_operators number_theoretical polynomials sums transcendental )
element_count=${#subdirs[@]}

index=0

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $XML_DIR/strings.xml
echo "<resources>" >> $XML_DIR/strings.xml

while [ "$index" -lt "$element_count" ]
do
    # Write the text files, extracting just the prototyes from /src/functions files.
    # Note: This could be cleaner, but it works.
    egrep "Help*" $PARI/src/functions/${subdirs[$index]}/* > ${subdirs[$index]}_tmp.txt
    egrep -o ' \<[a-z].*\>\W*\W[::]' ${subdirs[$index]}_tmp.txt > ${subdirs[$index]}_proto_tmp.txt
    sed -e 's/[::]//' ${subdirs[$index]}_proto_tmp.txt > ${subdirs[$index]}_proto.txt
    awk '{sub(/^[ \t]+/, "")};1' ${subdirs[$index]}_proto.txt > $PROTO_DIR/${subdirs[$index]}_proto.txt
    # Now copy everything from the text files into correct format for string.xml
    echo "<string-array name=\"${subdirs[$index]}\">" >> $XML_DIR/strings.xml
    sed -e 's/^/\<item\>/' $PROTO_DIR/${subdirs[$index]}_proto.txt | sed -e 's/$/\<\/item\>/' >> $XML_DIR/strings.xml
    echo "</string-array>" >> $XML_DIR/strings.xml
    let "index = $index + 1"
done

echo "</resources>" >> $XML_DIR/strings.xml
rm -f *.txt
rm -f $PROTO_DIR/*.txt
echo "Done!"
#EOF
