package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符，将敏感词替换为***
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    //2.这个类的实例对象在被访问之前就已经根据敏感词文件初始化了前缀树
    //这个注解表示，容器在实例化这个类的对象，调用构造方法之后，会自动调用这个init方法
    @PostConstruct
    public void init() {
        try (
                //根据文件获得字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //把字节流转为字符，读取比较方便
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 读一行得到一个敏感词，将其添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            //过滤空格
            if (isSymbol(c)) {
                continue;
            }
            //查找根节点有没有字符为c的子节点
            TrieNode subNode = tempNode.getSubNode(c);

            //没有，就把该节点和字符加入到根节点的map中，作为根节点的子节点
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // tempNode指向当前循环的字符对应的节点,进入下一轮循环，遍历下一个字符
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    //3.传进来的是待过滤的字符串，返回的是将敏感词替换后的字符串
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int low = 0;
        // 指针3
        int fast = 0;
        // 记录结果
        StringBuilder sb = new StringBuilder();

        while (fast < text.length()) {
            char c = text.charAt(fast);

            // 跳过空格
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    low++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                fast++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(low));
                // 进入下一个位置
                fast = ++low;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                low = ++fast;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 继续检查下一个字符
                fast++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(low));

        return sb.toString();
    }

    // 判断是否为符号，比如星啊什么的用来干扰的，不是文字的
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 1.前缀树，定义前缀树的数据结构
    private class TrieNode {

        // 关键词结束标识，为假就证明当前节点对应字符，不是关键词的结尾字符，为真代表是结尾字符
        private boolean isKeywordEnd = false;

        // 节点(key是当前节点子节点的字符,value是字符对应的子节点)
        // 节点不存储自己对应的字符，只存储子节点对应的字符
        // 节点对应的字符存储在父节点中
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        //默认所有节点对应的字符都不是关键词结尾字符
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        //只有在根据关键词初始化前缀树的时候，根据判断，将标记设置为true，
        //代表当前节点对应字符是结尾字符
        public void setKeywordEnd(boolean Flag) {
            isKeywordEnd = Flag;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }

}
