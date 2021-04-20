

# 67、剪绳子

* 暴力递归

  ```java
  class Solution {
      public int cuttingRope(int n) {
          if(n == 2){
              return 1;
          }
          if(n == 3){
              return 2;
          }
          return dfs(n);
      }
  
      public int dfs(int n){
          //dfs前面肯定已经被剪过，所以剩下的长度如果<=4,那么最大乘积结果n长度不减
          //与cuttingRope不同，进入方法还没有剪过，必须要先剪一段
          if(n <= 4){
              return n;
          }
          int res = 0;
          //循环减去一段长度，求乘积最大值
          for(int i = 1;i < n; i++){
              res = Math.max(res, i * dfs(n-i));
          }
          return res;
      }
  }
  ```

  时间复杂度O(ni): 循环次数n * (n-1) * (n-2)...

  空间复杂度O(n):   最长递归次数n，每次只剪1m

* 记忆化递归

  暴力递归会导致重复计算：

  ![](https://uploadfiles.nowcoder.com/images/20200523/284295_1590216999783_2CC2B62A31846CE8FC9AB8E71A5EB53D)

  计算过程用数组存起来

  ```java
  class Solution {
      public int cuttingRope(int n) {
          if(n == 2){
              return 1;
          }
          if(n == 3){
              return 2;
          }
          int[] temp = new int[n+1];
          return dfs(n, temp);
      }
  
      public int dfs(int n, int[] temp){
          if(n <= 4){
              return n;
          }
          if(temp[n] > 0){
              return temp[n];
          }
          int res = 0;
          for(int i = 1;i < n; i++){
              res = Math.max(res, i * dfs(n-i, temp));
          }
          //数组保存临时值
          temp[n] = res;
          return res;
      }
  }
  ```

  时间复杂度O(n2)，怎么来的看下面这种解法

  空间复杂度O(n)

* 动态规划

  由记忆化递归演变迭代方式

  ```java
  class Solution {
      public int cuttingRope(int n) {
          if(n == 2){
              return 1;
          }
          if(n == 3){
              return 2;
          }
          int[] temp = new int[n+1];
          for(int i = 1; i<= 4; i++){
              temp[i] = i;
          }
          for(int i = 5; i <= n; i++){
              //计算temp[5],6,7...n
              for(int j = 1; j < i; j++){
                  //每次剪1，2，3...
                  //因为是从小算到大的，不同于递归当时从大到小，所以temp[i-j]总是计算过后的值
                  temp[i] = Math.max(temp[i], j * temp[i - j]);
              }
          }
          return temp[n];
      }
  }
  ```

  时间复杂度O(n2): i等于n的时候，要循环n * n-1次

  空间复杂度O(n)



# 66、机器人的运动范围

* ```java
  class Solution {
      int[][] next = {{0,1},{1,0},{0,-1},{-1,0}};
      public int movingCount(int m, int n, int k) {
          if(m <= 0 || n <= 0){
              return 0;
          }
          boolean[][] mark = new boolean[m][n];
          return dfs(0,0,m,n,mark,k);
          
      }
  
      public int dfs(int row, int col, int m, int n,boolean[][] mark, int k){
          //检查越界
          if(row < 0 || row >= m || col < 0 || col >= n){
              return 0;
          }
          //检查访问过
          if(mark[row][col]){
              return 0;
          }
          //检查是否符合条件
          if(getSum(row, col) > k){
              return 0;
          }
          int res = 0;
          //当前节点可行
          res++;
          mark[row][col] = true;
          for(int[] temp : next){
              //下一步，上下左右
              res += dfs(row + temp[0], col + temp[1], m, n, mark, k);
          }
          return res;
      }
  
      public int getSum(int row, int col){
          int res = 0;
          while(row != 0){
              res += row % 10;
              row /= 10;
          }
          while(col != 0){
              res += col % 10;
              col /= 10;
          }
          return res;
      }
  }
  ```

  时间复杂度O(mn)，每个格子都被遍历一次

  空间复杂度 O(mn)

# 65、矩阵中的路径

* ```java
  class Solution {
      int[][] next = {{0,1}, {1,0},{0,-1},{-1,0}};
      public boolean exist(char[][] board, String word) {
          if(board != null && board.length > 0 && board[0] != null){
              int m = board.length;
              int n = board[0].length;
              boolean[][] visit = new boolean[m][n];
              for(int i = 0; i < m; i++){
                  for(int j = 0; j < n; j++){
                      //遍历每一个点作为起点
                      if(dfs(i,j,m,n,0,board,word,visit)){
                          return true;
                      }
                  }
              }
  
          }
          return false;
      
      }
  
      //row 行坐标
      //col 列坐标
      //m , n矩阵的行数和列数
      //index 字符串将要将要对比的是否字符相同的下标
      public boolean dfs(int row, int col, int m, int n, int index,char[][] board, String word, boolean[][] visit){
          //首先根据inddex是否结束递归，而不是是否越界
          //比如当前index刚好是word最后一个字符，且匹配上了。这时候又在边界，如果先根据越界判断，则会误判为false
          if(index >= word.length()){
              return true;
          }
          //越界判断
          if(row < 0 || row >= m || col < 0 || col >= n || visit[row][col]){
              return false;
          }
          //不匹配判断
          if(board[row][col] != word.charAt(index)){
              return false;
          }
          //当前值匹配
          visit[row][col] = true;
          for(int[] step : next){
              if(dfs(row+step[0], col+step[1],m,n,index+1,board,word,visit)){
                  //因为这里只需要判断有没有一条路径，不需要找出多条路径
                  //所以visit不需要置false
                  return true;
              }
          }
          visit[row][col] = false;
          return false;
      }
  }
  ```

  M,N分别是矩阵长宽，K是字符串长度

  时间复杂度：O(3<sup>k</sup> * MN)。

  ​	矩阵每个节点都可能作为起点，O(MN)

  ​	对于每个起点，最坏都需要遍历长度为K的字符串，对于每一个字符，都可能有上下左右四个方向。反咬舍弃掉从上一个字符来的那个方向,因为被访问过 	     了，只剩下3个方向。O(3<sup>K</sup>)

  空间复杂度：O(K),递归深度K，最坏O(MN)。但是这里用了visit数组，所以是O(MN)

# 64、滑动窗口的最大值

* 暴力遍历

  ```java
  class Solution {
      public int[] maxSlidingWindow(int[] nums, int k) {
          if(nums == null || nums.length == 0 || k > nums.length){
              return new int[0];
          }
          int[] max = new int[nums.length - k + 1];
          int index = 0;
          for(int i = 0; i < nums.length - k + 1; i++){
              int temp = nums[i];
              for(int j = i+1; j < i+k; j++){
                  if(nums[j] > temp){
                      temp = nums[j];
                  }
              }
             max[index++] = temp;
          }
          return max;
      }
  }
  ```

  时间复杂度：O(nk),两层循环

  空间复杂度：O(1),存结果用的数组不算额外空间

* 优先队列

  与暴力解法时间复杂度相差不大，还得提供额外空间，因此不采用

* 单调栈（双端队列）

  ```java
  class Solution {
      public int[] maxSlidingWindow(int[] nums, int k) {
          if(nums == null || nums.length == 0 || k > nums.length){
              return new int[0];
          }
          Deque<Integer> dequeue = new LinkedList<>();
          int[] res = new int[nums.length - k + 1];
          int index = 0;
          //先建立一个滑动窗口
          for(int i = 0; i < k; i++){
              //从队尾插入，把小于当前的数都移走
              //取滑动窗口最大值的时候就取队头就可以了
              while(!dequeue.isEmpty() && dequeue.peekLast() < nums[i]){
                  dequeue.pollLast();
              }
              dequeue.addLast(nums[i]);
          }
          res[index++] = dequeue.peekFirst();
          //遍历后面的滑动窗口
          for(int i = 1; i < nums.length - k + 1; i++){
              if(dequeue.peekFirst() == nums[i-1]){
                  dequeue.pollFirst();
              }
              //在以i开头的滑动窗口，只有最后一个元素还没有添加进dequue
              while(!dequeue.isEmpty() && dequeue.peekLast() < nums[i+k-1]){
                  dequeue.pollLast();
              }
              dequeue.addLast(nums[i+k-1]);
              res[index++] = dequeue.peekFirst();
          }
          return res;
      }
  }
  ```

  时间复杂度O(n),从dequue取队头是O(1),最坏情况下，是一个已经有序的窗口，再添加一个元素比他们都大，需要移除窗口的所有元素。时间复杂度是O(nk)

  空间复杂度O(k),dequeue最多存储了一个窗口的所有值

  