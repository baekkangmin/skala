count=0
echo "루프에서 count 증가시키기"
for i in 1 2 3 4 5; do
	((count+=1))
done

echo "최종 count 값 = $count"
echo

# for 루프
for (( i=0; i<5; i++ )); do
echo "i = $i"
done
