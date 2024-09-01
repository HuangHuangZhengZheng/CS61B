import java.util.ArrayDeque;

public class Palindrome {
    public Deque<Character> wordToDeque(String word){
        Deque<Character> deque = new LinkedListDeque<>();
        for (int i = 0; i < word.length(); i++) {
            Character c = word.charAt(i);
            deque.addLast(c);
        }
        return deque;
    }
    private boolean isPalindrome(Deque<Character> deque){
        if (deque.isEmpty() || deque.size() == 1){
            return true;
        }
        Character h = deque.removeFirst();
        Character t = deque.removeLast();
        if (!h.equals(t)) {
            return false;
        }else{
            return isPalindrome(deque);
        }
    }
    public boolean isPalindrome(String word){
        // recursion and not using "get"!
        Deque<Character> deque = wordToDeque(word);
        return isPalindrome(deque);
    }

    private boolean isPalindrome(Deque<Character> deque, CharacterComparator cc){
        if (deque.isEmpty() || deque.size() == 1) {
            return true;
        }
        Character h = deque.removeFirst();
        Character t = deque.removeLast();
        if (!cc.equalChars(h,t)){
            return false;
        }else{
            return isPalindrome(deque, cc);
        }
    }

    public boolean isPalindrome(String word, CharacterComparator cc){
        Deque<Character> deque = wordToDeque(word);
        return isPalindrome(deque, cc);
    }
}
