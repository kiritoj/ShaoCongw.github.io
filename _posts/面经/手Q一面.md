## 1、activity的启动流程

## 2、binder机制

## 3、invalidate重绘会调用哪些流程

https://blog.csdn.net/hello_json/article/details/79616226

## 4、简单四则运算

* 无括号（leetcode 227）

  ```java
  class Solution {
      public int calculate(String s) {
          if(s == null || s.length() == 0){
              return 0;
          }
          Stack<Integer> stack1 = new Stack<>();
          Stack<Character> stack2 = new Stack();
          HashMap<Character, Integer> map = new HashMap<>();//存储操作符对应的优先级
          map.put('+',1);
          map.put('-',1);
          map.put('*',2);
          map.put('/',2);
          for(int i = 0; i < s.length(); i++){
              //空格处理
              if(s.charAt(i) == ' '){
                  continue;
              }
              if(isNumber(s.charAt(i))){
                  //操作数直接入栈,可能是多位数
                  int sum = s.charAt(i) - '0';
                  int j = i+1;
                  for(; j < s.length(); j++){
                      if(isNumber(s.charAt(j))){
                          int num = s.charAt(j) - '0';
                          sum = sum * 10 + num;
                      }else{
                          //因为i会自增，所以要减1
                          break; //跳出当前循环
                      }
                  }
                  i = j-1; // i = j-1不要写在for循环中的else中，因为可能字符串只有一个数字：“12345”，会一直走到底，这样i就得不到更新
                  stack1.push(sum);
              } else {
                  //操作符处理
                  if(stack2.isEmpty()){
                      //操作符栈为空直接入栈
                      stack2.push(s.charAt(i));
                  } else{
                      //当前操作符优先级如果比栈顶优先级低(同级也算)，则先计算优先级更高的。再插入当前优先级
                      while(!stack2.isEmpty() && map.get(s.charAt(i)) <= map.get(stack2.peek())){
                          int num1 = stack1.pop();
                          int num2 = stack1.pop(); //num2才是第一个操作数，计算的时候要在前面
                          char option = stack2.pop();
                          int res = calculate(num1, num2, option);
                          stack1.push(res);
                      }
                      stack2.push(s.charAt(i));
                  }
              }
          }
          while(!stack1.isEmpty() && !stack2.isEmpty()){
              int num1 = stack1.pop();
              int num2 = stack1.pop();
              char option = stack2.pop();
              int res = calculate(num1, num2, option);
              stack1.push(res);
          }
          return stack1.pop();
      }
  
      public int calculate(int num1, int num2, char option){
          int res = 0;
          switch(option){
              case '+':
                  res = num2 + num1;
                  break;
              case '-':
                  res = num2 - num1;
                  break;
              case '*':
                  res = num2 * num1;
                  break;
              case '/':
                  res = num2 / num1;
          }
          return res;
      }
  
      public boolean isNumber(char c){
          return c >= '0' && c <= '9';
      }
  }
  
  ```

* 有括号（leetcode 224）

  在上面的流程中增加括号处理就可以了

  ```java
  class Solution {
      public int calculate(String s) {
          if(s == null || s.length() == 0){
              return 0;
          }
          Stack<Integer> stack1 = new Stack<>();
          Stack<Character> stack2 = new Stack();
          HashMap<Character, Integer> map = new HashMap<>();//存储操作符对应的优先级
          map.put('(',0);
          map.put('+',1);
          map.put('-',1);
          map.put('*',2);
          map.put('/',2);
          for(int i = 0; i < s.length(); i++){
              //空格处理
              if(s.charAt(i) == ' '){
                  continue;
              }
              if(isNumber(s.charAt(i))){
                  //操作数直接入栈,可能是多位数
                  int sum = s.charAt(i) - '0';
                  int j = i+1;
                  for(; j < s.length(); j++){
                      if(isNumber(s.charAt(j))){
                          int num = s.charAt(j) - '0';
                          sum = sum * 10 + num;
                      }else{
                          //因为i会自增，所以要减1
                          break; //跳出当前循环
                      }
                  }
                  i = j-1; // i = j-1不要写在for循环中的else中，因为可能字符串只有一个数字：“12345”，会一直走到底，这样i就得不到更新
                  stack1.push(sum);
              } else {
                  //操作符处理
                  if(stack2.isEmpty()){
                      //操作符栈为空直接入栈
                      stack2.push(s.charAt(i));
                  } else if(s.charAt(i) == ')'){
                      //处理遇到)的情况，遇到这个右括号，就要把前面左括号的之间的先计算了
                      while(!stack2.isEmpty() && stack2.peek() != '('){
                          int num1 = stack1.pop();
                          int num2 = stack1.pop();
                          char option = stack2.pop();
                          int res = calculate(num1, num2, option);
                          stack1.push(res);
                      }
                      //计算完后，弹出左括号，右括号永远不入栈
                      if(stack2.peek() == '('){
                          stack2.pop();
                      }
                  } else{
                      //当前操作符优先级如果比栈顶优先级低(同级也算)，则先计算优先级更高的。再插入当前优先级
                      while(!stack2.isEmpty() && s.charAt(i) != '(' && map.get(s.charAt(i)) <= map.get(stack2.peek())){
                          int num1 = stack1.pop();
                          int num2 = stack1.pop(); //num2才是第一个操作数，计算的时候要在前面
                          char option = stack2.pop();
                          int res = calculate(num1, num2, option);
                          stack1.push(res);
                      }
                      stack2.push(s.charAt(i));
                  }
              }
          }
          while(!stack1.isEmpty() && !stack2.isEmpty()){
              int num1 = stack1.pop();
              int num2 = stack1.pop();
              char option = stack2.pop();
              int res = calculate(num1, num2, option);
              stack1.push(res);
          }
          return stack1.pop();
      }
  
      public int calculate(int num1, int num2, char option){
          int res = 0;
          switch(option){
              case '+':
                  res = num2 + num1;
                  break;
              case '-':
                  res = num2 - num1;
                  break;
              case '*':
                  res = num2 * num1;
                  break;
              case '/':
                  res = num2 / num1;
          }
          return res;
      }
  
      public boolean isNumber(char c){
          return c >= '0' && c <= '9';
      }
  }
  
  ```

  有问题：没有处理负数，会有空栈异常。**上一道题的条件有全部是非负数操作**，这一道题没有