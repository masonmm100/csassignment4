import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;

public class StrongPasswordChecker {

    static class HashTableSeparateChaining {
        private int size;
        private LinkedList<Entry>[] table;
        public int comparisons;

        static class Entry {
            String key;
            int value;

            Entry(String key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        @SuppressWarnings("unchecked")
        public HashTableSeparateChaining(int size) {
            this.size = size;
            this.table = new LinkedList[size];
            for (int i = 0; i < size; i++) {
                table[i] = new LinkedList<>();
            }
        }

        private int hash(String key, HashFunction hashFunc) {
            return Math.abs(hashFunc.hash(key) % size);
        }

        public void insert(String key, int value, HashFunction hashFunc) {
            int index = hash(key, hashFunc);
            for (Entry entry : table[index]) {
                if (entry.key.equals(key)) {
                    return;
                }
            }
            table[index].add(new Entry(key, value));
        }

        public boolean search(String key, HashFunction hashFunc) {
            comparisons = 0;
            int index = hash(key, hashFunc);
            for (Entry entry : table[index]) {
                comparisons++;
                if (entry.key.equals(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class HashTableLinearProbing {
        private int size;
        private Entry[] table;
        public int comparisons;

        static class Entry {
            String key;
            int value;

            Entry(String key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        public HashTableLinearProbing(int size) {
            this.size = size;
            this.table = new Entry[size];
        }

        private int hash(String key, HashFunction hashFunc) {
            return Math.abs(hashFunc.hash(key) % size);
        }

        public void insert(String key, int value, HashFunction hashFunc) {
            int index = hash(key, hashFunc);
            for (int i = 0; i < size; i++) {
                int probeIndex = (index + i) % size;
                if (table[probeIndex] == null || table[probeIndex].key.equals(key)) {
                    table[probeIndex] = new Entry(key, value);
                    return;
                }
            }
        }

        public boolean search(String key, HashFunction hashFunc) {
            comparisons = 0;
            int index = hash(key, hashFunc);
            for (int i = 0; i < size; i++) {
                comparisons++;
                int probeIndex = (index + i) % size;
                if (table[probeIndex] == null) {
                    return false;
                }
                if (table[probeIndex].key.equals(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    interface HashFunction {
        int hash(String key);
    }

    static class OldHashCode implements HashFunction {
        @Override
        public int hash(String key) {
            int hash = 0;
            int skip = Math.max(1, key.length() / 8);
            for (int i = 0; i < key.length(); i += skip) {
                hash = (hash * 37) + key.charAt(i);
            }
            return hash;
        }
    }

    static class CurrentHashCode implements HashFunction {
        @Override
        public int hash(String key) {
            int hash = 0;
            for (int i = 0; i < key.length(); i++) {
                hash = (hash * 31) + key.charAt(i);
            }
            return hash;
        }
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> dictionary = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(StrongPasswordChecker.class.getResource("wordlist.10000").getPath()))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim());
                lineNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize hash tables
        HashTableSeparateChaining hashTableChain = new HashTableSeparateChaining(1000);
        HashTableLinearProbing hashTableProbe = new HashTableLinearProbing(20000);

        OldHashCode oldHash = new OldHashCode();
        CurrentHashCode currentHash = new CurrentHashCode();

        for (int i = 0; i < dictionary.size(); i++) {
            String word = dictionary.get(i);
            hashTableChain.insert(word, i + 1, oldHash);
            hashTableProbe.insert(word, i + 1, oldHash);
        }

        String[] passwords = {
                "account8",
                "accountability",
                "9a$D#qW7!uX&Lv3zT",
                "B@k45*W!c$Y7#zR9P",
                "X$8vQ!mW#3Dz&Yr4K5"
        };

        for (String password : passwords) {
            System.out.println("Checking password: " + password);

            boolean isStrong = password.length() >= 8;
            for (String word : dictionary) {
                if (password.equals(word) || (password.startsWith(word) && password.substring(word.length()).matches("\\d"))) {
                    isStrong = false;
                    break;
                }
            }

            System.out.println("Password strong: " + isStrong);

            hashTableChain.search(password, oldHash);
            System.out.println("Search cost (separate chaining, old hash): " + hashTableChain.comparisons);

            hashTableProbe.search(password, oldHash);
            System.out.println("Search cost (linear probing, old hash): " + hashTableProbe.comparisons);

            hashTableChain.search(password, currentHash);
            System.out.println("Search cost (separate chaining, current hash): " + hashTableChain.comparisons);

            hashTableProbe.search(password, currentHash);
            System.out.println("Search cost (linear probing, current hash): " + hashTableProbe.comparisons);

            System.out.println();
        }
    }
}
