

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

