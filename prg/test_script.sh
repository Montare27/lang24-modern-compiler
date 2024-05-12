
make clean > /dev/null && make > /dev/null

for test_file in ./tests/*; do
    filename=$(basename "$test_file")
    dirname=$(dirname "$test_file")

    if [[ $filename != *.lang24 ]]; then
        continue
    fi

    no_suff="${filename%.lang24}"
    out="${dirname}/${no_suff}_izhod.txt"
    in="${dirname}/${no_suff}_vhod.txt"

    if [ ! -f "${out}" ]; then
        echo "Passing $filename because no expected output"
        continue
    fi

    echo "Testing $filename"
    rm -f ./prg/temp831.txt > /dev/null

    cp "$test_file" ./prg/temp831.lang24
    if [ -f "${in}" ]; then
        make -C ./prg temp831 TARGETPHASE=all < "${in}" > ./prg/temp831.txt
    else
        make -C ./prg temp831 TARGETPHASE=all > ./prg/temp831.txt
    fi

    sed -e '1,/:-) This is LANG'"'"'24 compiler:/d' -e '/EXIT CODE: -\?[0-9]\+/,$d' ./prg/temp831.txt > temp123.out && mv temp123.out ./prg/temp831.txt

    if ! diff ./prg/temp831.txt "$out" > /dev/null; then
        cat "$out"
        cat ./prg/temp831.txt
        echo "$out"
        diff ./prg/temp831.txt "$out"
        echo "Output of test $filename is different than expected"
        exit 1
    fi
    echo "DONE"
    rm ./prg/temp831.txt

done

echo "All tests pass"