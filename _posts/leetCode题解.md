## leetCode

## 1.两数之和

**暴力**

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i+1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target){
                    return new int[]{i,j};
                }
            }
        }
        return new int[2];
    }
}
```

**运用hashMap辅助**

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        HashMap<Integer, Integer> map = new HashMap<>();
        //key是数值，value是下标
        for (int i = 0; i < nums.length; i++) {
           int key = target - nums[i];
           if (map.containsKey(key)){
               return new int[]{map.get(key), i};
           }
           map.put(nums[i], i);
        }
        return new int[2];
    }
}
```

**错误**：排序后位置变了，除非题目要求是找出这两个数，而不是他们的下标，不过可以用在三数之和的问题中

```
class Solution {
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        if(nums != null && nums.length > 1){
            Arrays.sort(nums);
            int left = 0;
            int right = nums.length - 1;
            while (left < right){
                if ((nums[left] + nums[right]) == target){
                    result[0] = left;
                    result[1] = right;
                    break;
                }else if ((nums[left] + nums[right] < target)){
                    left++;
                }else{
                    right--;
                }
            }
        }
        return result;
    }
}
```

## 2.两数相加

注意两个数长度不对称的情况，以及最后一位进位为1，要再添加一个节点

```java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode fakeHead = new ListNode(0);
        ListNode cur = fakeHead;
        int carry = 0;
        while (l1 != null || l2 != null){
            int x = l1 != null ? l1.val : 0;
            int y = l2 != null ? l2.val : 0;
            int sum = x + y + carry;
            carry = sum / 10;
            ListNode node = new ListNode(sum % 10);
            cur.next = node;
            cur = cur.next;
            if (l1 != null){
                l1 = l1.next;
            }
            if (l2 != null){
                l2 = l2.next;
            }
        }
        if (carry == 1){
            cur.next = new ListNode(1);
        }
        return fakeHead.next;
    }
}
```

## 3.无重复字符的最长子串长度

用hashset判断是否有重复子串

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int result = 0;
        if (s != null && s.length() > 0) {
            HashSet<Character> set = new HashSet<>();
            int right = 1;
            set.add(s.charAt(0));
            for (int i = 0; i < s.length(); i++) {
                if (i > 0) {
                    set.remove(s.charAt(i - 1));
                }
                while (right < s.length() && !set.contains(s.charAt(right))) {
                    set.add(s.charAt(right));
                    right++;
                }
                result = Math.max(result, right - i);

            }
        }
        return result;
    }
}
```

## 4.两个正序数组的中位数

**合并数组再求**



**不合并直接求**

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int length = nums1.length + nums2.length;
        if (length % 2 == 1){
            return getMid(nums1, nums2, length / 2);
        }else{
            return (getMid(nums1, nums2, length / 2 - 1) + getMid(nums1, nums2, length / 2))/2.0;
        }
    }
    public int getMid(int[] nums1, int[] nums2, int k){
        int index1 = 0;
        int index2 = 0;
        int flag = 0;
        int count = 0;
        while ((index1 < nums1.length || index2 < nums2.length) && count <= k){
            if (index1 >= nums1.length){
                index2++;
                flag = 2;
            }else if (index2 >= nums2.length){
                index1++;
                flag = 1;
            }else {
                if (nums1[index1] <= nums2[index2]) {
                    index1++;
                    flag = 1;
                } else {
                    index2++;
                    flag = 2;
                }
            }
            count++;
        }
        return flag == 1 ? nums1[index1-1] : nums2[index2-1];
    }
}
```

## 5.最长回文子串

```java
class Solution {
    public String longestPalindrome(String s) {
        if (s == null || s.length() <= 1) {
            return s;
        }
        boolean[][] bool = new boolean[s.length()][s.length()];
        String result = "";
        //S[I,J]是否是回文
        //外层要想控制右边j的增长，如果先控制i的增长，当s[0,6]的时候，s[1,5]还没判断
        for (int j = 0; j < s.length(); j++) {
            for (int i = 0; i <= j; i++) {
                if (i == j ) {
                    //单个字符一定是回文
                    bool[i][j] = true;
                }else if (i + 1 == j){
                    //相邻的两个字符只要一样就是回文
                    bool[i][j] = s.charAt(i) == s.charAt(j);
                }else{
                    //i和j相差多个单位
                    //"cabac",aba是回文，两端字符相同就也是回文
                    bool[i][j] = bool[i + 1][j - 1] && s.charAt(i) == s.charAt(j);
                }
                if (bool[i][j] && (j - i + 1) > result.length()) {
                    //比较当前回文字符串和result的长度，更新
                    result = s.substring(i, j + 1);
                }
            }
        }
        return result;
    }
}
```

## 6.Z字形变换

```java
class Solution {
    public String convert(String s, int numRows) {
        if(numRows == 1){
            return s;
        }
        List<StringBuilder> list = new ArrayList<>();
        for(int i = 0; i < Math.min(s.length(), numRows); i++){
            //numRows可能比字符串长度还大，只需创建s.length个sb
            list.add(new StringBuilder());
        }
        boolean next = false; //是否换行
        int curRow = 0; //当前行
        for(char c : s.toCharArray()){
            list.get(curRow).append(c);
            if(curRow == 0 || curRow == numRows - 1){
                //当第一行和最后一行的时候，要改变一次方向
                next = !next;
            }
            curRow += next ? 1 : -1;
        }
        StringBuilder res = new StringBuilder();
        for(StringBuilder builder : list){
            res.append(builder);
        }
        return res.toString();
    }
}
```

## 7.整数反转

处理好正数和负数的越界

```java
class Solution {
    public int reverse(int x) {
        int pop = 0;
        int res = 0;
        while(x != 0){
            pop = x % 10;
            x /= 10;
            if(res > Integer.MAX_VALUE / 10 ||(res == Integer.MAX_VALUE / 10 && pop > 7)){
                //正数越界
                return 0;
            }
            if(res < Integer.MIN_VALUE / 10 ||(res == Integer.MIN_VALUE / 10 && pop < -8)){
                //负数越界
                return 0;
            }
            res = res * 10 + pop;
        }
        return res;
    }
}
```

## 8.字符串转整数

```java
class Solution {
    public int myAtoi(String str) {
        if (str == null || str.length() == 0){
            return 0;
        }
        boolean isNegative  = false;//是否是负数标记
        int result = 0;
        int start = 0;
        //除去空格，第一个非空字符的位置
        while (start < str.length() && str.charAt(start) == ' '){
            start++;
        }
        for (int i = start; i < str.length(); i++) {
            if (i == start){
                if (str.charAt(i) == '+' || str.charAt(i) == '-'){
                    isNegative = str.charAt(i) == '-';
                }else if (str.charAt(i) >= '0' && str.charAt(i) <= '9'){
                    result = result * 10 + str.charAt(i) - '0';
                }else{
                    //第一个非空字符是非法字符，无法转换，返回0
                    return 0;
                }
                continue;
            }

            if (str.charAt(i) >= '0' && str.charAt(i) <= '9'){
                int num = str.charAt(i) - '0';
                if (!isNegative) {
                    if (result > Integer.MAX_VALUE / 10 ||(result == Integer.MAX_VALUE / 10 && num > 7)){
                        //正数溢出判断
                        return  Integer.MAX_VALUE;

                    }
                }else{
                    if(-result < Integer.MIN_VALUE / 10 ||(-result == Integer.MIN_VALUE / 10 && -num < -8)){
                        return  Integer.MIN_VALUE;

                    }
                }
                result = result * 10 + num;
            }else{
                //在后面遇到了非法字符，遍历结束
                break;
            }
        }
        return isNegative ? -result : result;
    }
}
```

## 9.回文数

常规方法：

```java
class Solution {
    public boolean isPalindrome(int x) {
        if (x < 0||(x %10 == 0 && x != 0)){
            //负数一定不是回文数
            //能够整除10的回文只有0
            return false;
        }
        List<Integer> list = new ArrayList();
        int temp = x;
        int num = 0;
        while(temp != 0){
            num = temp % 10;
            temp = temp / 10;
            list.add(num);
        }

        for(int a : list){
            temp = temp * 10 + a;
        }
        return temp == x;
    }
}
```

逆转一半的数字

```java
class Solution {
    public boolean isPalindrome(int x) {
        if (x < 0 || (x % 10 == 0 && x != 0)){
            //负数一定不是回文数
            //能够整除10的回文只有0
            return false;
        }
        int reverseNum = 0;
        while (x > reverseNum){
            reverseNum = reverseNum * 10 + x % 10;
            x /= 10;
        }
        //如果是偶数，x == reversNum
        //如果是奇数，reverseNum会比x多一位中间的数，把它去除掉（/10）
        return x == reverseNum || x == reverseNum / 10;
    }
}
```

## 10.正则表达式匹配

不懂。。。。

```java
class Solution {
    public boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        if (s == null || p == null || n == 0){
            return false;
        }
        boolean[][] f = new boolean[m+1][n+1];
        //f[i][j] 代表s[0,i)与p[0,j)匹配是否匹配
        f[0][0] = true; //代表两个空串可以匹配
        for(int i = 0; i <= m;i++){
            for(int j = 1; j <=n; j++){
                if(p.charAt(j - 1) == '*'){
                    if(match(s,p,i,j-1)){
                        f[i][j] = f[i][j-2] || f[i-1][j];
                    }else{
                        f[i][j] = f[i][j-2];
                    }
                }else{
                    if(match(s, p, i, j)){
                        f[i][j] = f[i - 1][j - 1];
                    }
                }
            }
        }
        return f[m][n];

    }

    public boolean match(String s, String p, int i, int j){
        if (i == 0){
            return false;
        }
        if(p.charAt(j -1 ) == '.'){
            return true;
        }
        return s.charAt(i -1) == p.charAt(j - 1);
    }
}
```

## 16.最接近的三数之和

```java
class Solution {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int result = 100000;
        for(int i = 0; i < nums.length - 2; i++){
            if(i > 0 && nums[i] == nums[i - 1]){
                continue;
            }

            int j = i+1;
            int k = nums.length - 1;
            while(j < k){
                int sum = nums[i] + nums[j] + nums[k];
                if(sum == target){
                    return target;
                }
                result = Math.abs(sum - target) < Math.abs(result - target) ? sum : result;
                if(sum < target){
                    j++;
                    while(j < k && nums[j] == nums[j-1]){
                        j++;
                    }
                }else{
                    k--;
                    while(j < k && nums[k] == nums[k+1]){
                        k--;
                    }
                }
            }
        }
        return result;
    }
}
```

## 19.删除链表第n个节点

```java
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode temp = new ListNode(0);
        temp.next = head;
        ListNode node1 = temp;
        ListNode node2 = temp;

        for(int i = 0; i < n+1; i++){
            node2 = node2.next;
        }
        while(node2 != null){
            node1 = node1.next;
            node2 = node2.next;
        }
        node1.next = node1.next.next;
        return temp.next;

    }
    }
```

## 20.有效的括号

```java
class Solution {
    public boolean isValid(String s) {
        if(s.length() % 2 == 1){
            //奇数长度的字符串一定不是
            return false;
        }
        Map<Character, Character> map = new HashMap<>();
        //存储右括号
        map.put(')','(');
        map.put(']','[');
        map.put('}','{');
        Stack<Character> stack = new Stack<>();
        for(int i = 0; i < s.length(); i++){
            if(map.containsKey(s.charAt(i))){
                //判断是不是右括号
                if(stack.isEmpty() || stack.peek() != map.get(s.charAt(i))){
                    //栈顶不存在可以匹配的左括号
                    return false;
                }
                stack.pop();//可以匹配，左括号出栈
            }else{
                //左括号直接入栈
                stack.push(s.charAt(i));
            }
        }
        return stack.isEmpty();
    }
}

```

## 22.生成括号

```java
class Solution {
    public List<String> generateParenthesis(int n) {
        List<String> list = new ArrayList<>();
        generate(new char[n*2],0,0,0,n,list);
        return list;
    }
    public void generate(char[] chars,int index,int left, int right,int n,List<String> list){
        if(index == chars.length){
            list.add(String.valueOf(chars));
            return;
        }
        if(left < n){
            //左括号的数量不能超过n
            chars[index] = '(';
            generate(chars,index+1, left+1, right, n, list);
        }
        if(right < left){
            //右括号的数量一定要小于左括号
            chars[index] = ')';
            generate(chars, index+1, left, right+1, n, list);
        }
    }
}
```

## 25.K个一组翻转链表

```java
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        //在头部新添加一个节点
        ListNode temp = new ListNode(0);
        temp.next = head;
        ListNode prev = temp;
        ListNode tail = temp;
        while(head != null){
            for(int i = 0; i < k; i++){
                tail = tail.next;
                //最后一组不足k个元素，不翻转
                if(tail == null){
                    return temp.next;
                }
            }
            ListNode next = tail.next; //下一个组的头结点
            reverse(head, tail);
            //翻转过后交换头、尾节点
            ListNode node = head;
            head = tail;
            tail = node;
            prev.next = head;
            tail.next = next;
            //移动到下一个组
            prev = tail;
            head = next;
        }
        return temp.next;
    }
    public void reverse(ListNode head, ListNode tail){
        //翻转head到tail的节点
        ListNode prev = head;
        ListNode cur = head.next;
        ListNode next = head;
        ListNode nextK = tail.next;
        while(cur != nextK){
            next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
    }
}
```

## 38.外观数列

```java
class Solution {
    public String countAndSay(int n) {
        if(n == 1){
            return "1";
        }
        //拿到上一层的字符串
        String str = countAndSay(n -1);
        int start = 0;
        StringBuilder builder = new StringBuilder();
        for(int i = 1;i <= str.length(); i++){
            if(i == str.length()){
                builder.append(i - start).append(str.charAt(start));
            }else if(str.charAt(i) != str.charAt(start)){
                builder.append(i - start).append(str.charAt(start));
                start = i;
            }
        }
        return builder.toString();
    }
}
```

## 39.组合总和

```java
class Solution {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> lists= new ArrayList<>();
        List<Integer> route = new ArrayList<>();
        dfs(candidates, target, 0, lists, route);
        return lists;
    }
    public void dfs(int[] candidates, int leftTarget, int index, List<List<Integer>> result, List<Integer> route){
        if (index == candidates.length){
            return;
        }
        if (leftTarget == 0){
            result.add(new ArrayList<>(route));
            return;
        }

        //不要当前这个数
        dfs(candidates, leftTarget, index+1, result, route);

        //加上当前这个数
        if(leftTarget - candidates[index] >= 0){
            route.add(candidates[index]);
            dfs(candidates, leftTarget - candidates[index], index, result, route);
            //数字可以重复选，所以index不用+1
            route.remove(route.size() - 1);
        }

    }
}
```

## 40.组合总和||

和39相比，每一个组合元素不可以重复使用

```java
class Solution {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList();
        List<Integer> path = new ArrayList();
        List<int[]> freq = new ArrayList<>(); //int[0]数组元素，int[1],该元素出现的次数
        Arrays.sort(candidates);
        for(int num : candidates){
            int size = freq.size();
            if(freq.isEmpty() || num != freq.get(size - 1)[0]){
                freq.add(new int[]{num,1});
            }else{
                freq.get(size - 1)[1]++;
            }
        }
        dfs(freq, target, 0, res, path);
        return res;
    }

    public void dfs(List<int[]> freq, int target, int index, List<List<Integer>> res, List<Integer> path){
        if(target  == 0){
            res.add(new ArrayList(path));
            return;
        }

        if(index == freq.size()){
            return;
        }

        //跳过当前元素
        dfs(freq, target, index+1, res, path);

        //如果该元素出现多次，如果全部使用可能会比target还大
        int count = Math.min(target / freq.get(index)[0], freq.get(index)[1]);
        for(int i = 1; i <= count; i++){
            path.add(freq.get(index)[0]);
            dfs(freq, target - i*freq.get(index)[0], index+1, res, path);
        }
        for(int i = 1; i <= count; i++){
            path.remove(path.size() - 1);
        }
    }
}
```

## 46，数组全排列（无重复元素）

```java
class Solution {
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> list = new ArrayList<>();
    public List<List<Integer>> permute(int[] nums) {
        if(nums != null && nums.length > 0){
            for(int num : nums){
                list.add(num);
            }
            permuteCore(nums, 0);
        }
        return res;

    }
    public void permuteCore(int[] nums, int index){
        if(index == nums.length){
            res.add(new ArrayList(list));
            return;
        }
        for(int i = index; i < nums.length; i++){
                Collections.swap(list,i, index);
                permuteCore(nums, index+1);
                //换回来
                Collections.swap(list,i, index);
        }
    }
}
```

## 47 数组全排列，有重复

```java
class Solution {
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> list = new ArrayList<>();

    public List<List<Integer>> permuteUnique(int[] nums) {
        if (nums != null && nums.length > 0) {
            for (int num : nums) {
                list.add(num);
            }
            permuteCore(nums, 0);
        }
        return res;
    }

    public void permuteCore(int[] nums, int index) {
        if (index == nums.length) {
            res.add(new ArrayList(list));
            return;
        }
        Map<Integer, Boolean> map = new HashMap<>();
        for (int i = index; i < nums.length; i++) {
            if (!map.containsKey(list.get(i))) {
                Collections.swap(list, i, index);
                permuteCore(nums, index + 1);
                //换回来
                Collections.swap(list, i, index);
                map.put(list.get(i), true);
            }

        }
    }
}
```

## 139.单词拆分

**动态规划**

```java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode fakeHead = new ListNode(0);
        ListNode cur = fakeHead;
        int carry = 0;
        while (l1 != null || l2 != null) {
            int x = l1 != null ? l1.val : 0;
            int y = l2 != null ? l2.val : 0;
            int sum = x + y + carry;
            carry = sum / 10;
            ListNode node = new ListNode(sum % 10);
            cur.next = node;
            cur = cur.next;
            if (l1 != null) {
                l1 = l1.next;
            }
            if (l2 != null) {
                l2 = l2.next;
            }
        }
        if (carry == 1) {
            cur.next = new ListNode(1);
        }
        return fakeHead.next;
    }
}
```

dp[i]表示s[0,i)是否可以被拆分，如果dp[j] == true ,s[j,i)也在单词列表中找得到，说明s[0,i)也可以被拆分

## 140. 单词拆分2

**暴力解法**

```java
class Solution {
    public List<String> wordBreak(String s, List<String> wordDict) {
        List<String> list = new ArrayList<>();
        if (s != null && s.length() > 0) {
            list = wordBreakCore(s, wordDict, 0);
        }
        return list;
    }

    public List<String> wordBreakCore(String s, List<String> wordDict, int start) {
        List<String> result = new ArrayList<>();
        if (start >= s.length()) {
            result.add("");
        }
        for (int end = start + 1; end <= s.length(); end++) {
            String str = s.substring(start, end);
            if (wordDict.contains(str)) {
                List<String> list = wordBreakCore(s, wordDict, end);
                for (String l : list) {
                    //这里不要用str+= ，这样下一次循环会受影响
                     str = s.substring(start, end) + (l.equals("") ? "" : " ") + l;
                    result.add(str);
                }
            }
        }
        return result;
        
    }
```



**记忆化优化**

暴力最大的缺点就是每次递归会有很多重复的计算，用Hashmap存起来，避免多次计算

```java
HashMap<Integer, List<String>> map = new HashMap<>();
```

```JAVA
public List<String> wordBreakCore(String s, List<String> wordDict, int start) {
    if (map.containsKey(start)){
        return map.get(start);

    }
    //****
    map.put(start, result);
    return result;
    }
```



**动态规划**

参考139题

```java
class Solution {
    public List<String> wordBreak(String s, List<String> wordDict) {
        if (s != null && s.length() > 0){
            List<String>[] dp = new ArrayList[s.length()+1];
            List<String> init = new ArrayList<>();
            init.add("");
            dp[0] = init;
            for (int i = 1; i <= s.length(); i++) {
                List<String> list = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    if(dp[j].size() > 0 && wordDict.contains(s.substring(j, i))){
                        for (String l : dp[j]) {
                            list.add(l + (l.equals("") ? "" : " ") + s.substring(j, i));
                        }
                    }
                }
                dp[i] = list;
            }
            return dp[s.length()];
        }
        return new ArrayList<>();
    }
}
```



## 216.组合总和

```java
class Solution {
    public List<List<Integer>> combinationSum3(int k, int n) {
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        dfs(k,n,1,res,path);
        return res;
    }
    public void dfs(int k, int n, int num, List<List<Integer>> res, List<Integer> path){
        if(n == 0 && k == 0){
            res.add(new ArrayList(path));
            return;
        }
        if(k == 0){
            return;
        }
        if(num > n || num > 9){
            return;
        }
        if(n - num >= 0){
            path.add(num);
            dfs(k - 1, n - num, num+1, res, path);
            path.remove(path.size() -1);
        }
        dfs(k, n, num+1, res, path);
    }
}
```

## 347.前K个高频元素

基本思路，用hashmap存储每个元素及它出现的次数

按次数从大到小排序，取前K个值

因此问题转化为了TopK问题

**快排思想**

```java
class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int i : nums){
            if(map.containsKey(i)){
                map.put(i, map.get(i)+1);
            }else{
                map.put(i, 1);
            }
        }
        Map.Entry<Integer, Integer>[] entrys = map.entrySet().toArray(new Map.Entry[map.size()]);
        quickSort(entrys, 0, entrys.length - 1, k);
        int[] result = new int[k];
        for(int i = 0; i < k; i++){
            result[i] = entrys[i].getKey();
        }
        return result;
    }

    public void quickSort(Map.Entry<Integer, Integer>[] entrys,int start, int end, int k){
        if(start < end){
            int i = getIndex(entrys, start, end);
            while(i != k-1){
                if(i < k-1){
                    i = getIndex(entrys,i+1,end);
                }else{
                    i = getIndex(entrys,start, i - 1);
                }
            }
        }
    }

    public int getIndex(Map.Entry<Integer, Integer>[] entrys, int start, int end){
        int i = start;
        int j = end;
        Map.Entry<Integer, Integer> temp = entrys[start];
        while(i < j){
            while(entrys[j].getValue() <= temp.getValue() && i < j){
                j--;
            }
            while(entrys[i].getValue() >= temp.getValue() && i < j){
                i++;
            }
            if(i < j){
                Map.Entry<Integer, Integer> entry = entrys[i];
                entrys[i] = entrys[j];
                entrys[j] = entry;
            }
        }
        entrys[start] = entrys[i];
        entrys[i] = temp;
        return i;

    }
}
```

**堆，适用于海量数据**



## 876.链表的中间节点

```java
class Solution {
    public ListNode middleNode(ListNode head) {
        if(head == null){
            return head;
        }
        ListNode slow = head;
        ListNode fast = head;
        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
}
```

如果有环呢？

先求出环的最后一个节点temp（不是入口节点）

然后条件判断改为fast ！=  temp



**三等分点**

fast每次走3步，条件改为

fast != null && fast.next != null && fast.next.next != null