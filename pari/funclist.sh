#!/bin/bash
PARI=./pari-dev
mkdir -p ./proto

subdirs=( conversions elliptic_curves linear_algebra number_fields operators programming symbolic_operators default number_theoretical member_functions polynomials sums transcendental )
element_count=${#subdirs[@]}

index=0

while [ "$index" -lt "$element_count" ]
do    # List all the elements in the array.
    egrep "Help*" $PARI/src/functions/${subdirs[$index]}/* > ${subdirs[$index]}_tmp.txt
    egrep -o ' \<[a-z].*\>\W*\W[::]' ${subdirs[$index]}_tmp.txt > ${subdirs[$index]}_proto_tmp.txt
    sed -e 's/[::]//' ${subdirs[$index]}_proto_tmp.txt > ${subdirs[$index]}_proto.txt
    awk '{sub(/^[ \t]+/, "")};1' ${subdirs[$index]}_proto.txt > ./proto/${subdirs[$index]}_proto.txt
    let "index = $index + 1"
done
rm -f *.txt