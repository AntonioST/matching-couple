matching-couple
===============

因接受了活動的委託，拜託幫忙寫個配對程式。

配對輸入為一群人，有著自己的性別(男、女、其他)，以及對方的性別(男、女、都喜歡)，
並且選擇不等的自己的特質、對方的特質、自己的興趣、對方的興趣，
依據上述內容進行交互比對，計算個別積分，挑選成功組合

批對輸出為成功配對(couple)的數目以及組合，以及未成功配對(left)的人名

由於是短時間內趕出來的程式，使用批配的演算法相當的粗糙，
免不了最後的人工調整，可當作是事先分類的工具

###編譯需求
* jdk 1.8 or higher

###執行需求
* jre 1.8 or higher

###執行準備檔案
* people list : in csv form (see example/people.csv)
* option list : 項目清單，記有預設所有項目
* option rule : 項目批對規則
* rule file : 積分計算規則

###執行方式
java -cp __classpath__ -jar Matching.jar __people_list__  __rule_file__  __output_file__

####people list
需為csv格式，除了方便使用報表軟體開啟一外，純文字檔案內容方便程式輸出、讀取

開頭第一行，格式
>name; self option1; target option1; self option2; target option2 ;...

name為名稱，是批對人們時使用的ID，必須相異。
self option為self列，是自己的相關訊息。
target option為target列，是對象的相關訊息。

除了第一列不看以外，程式為檢查之後所有列，
self列必須有，其名稱是之後找檔案的依據，
target列必須與self列一樣，或是空白，不然回報錯誤

第二行以及以後，及為愈批對人們的相關訊息，多個同類項目以','作為分隔

若有項目未再預設項目內，會在標準輸出中告知。

####option list
與people list置於相同資料夾，讀取people list時一起載入。
其中記有所有預設的項目

####option rule
項目批對規則，幾本上使用equal方法，
由於本程式沒有語意分析功能，例外規則必須自行補足。

規則表示：
> __左值__=__右值__ : __左值__(self) equal to __右值__(target)

> __左值__==__右值__ : __左值__(self) equal to __右值__(target) or __右值__(self) equal to __左值__(target)

> $=$ : 左值 equal 右值

> __左值__=[__右值__,__右值__,...] : __左值__ contain in 右值集合，左右可加換，或者兩邊皆為集合

> \#註解 ：忽略

####rule file
積分計算規則，其中以'$'開頭的關鍵字對應到項目。

格式如同程式碼，檔案內容是內嵌到javascript中，動態編譯，呼叫。

範例：
> $gender * ($attribute + $interest)

####output file
輸出檔案格式
>couple;__count__;score

>__person_1__;__person_2__;__score__

>[__couple__...]

>left;__count__

>__person__

>[__left__...]


