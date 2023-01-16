package practice2;

import java.util.Scanner;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;


public class I {
    public static void main(String... args) {
        Scanner in = new Scanner(System.in);
        String s = in.next();
        int[] sa = StringAlgorithm.suffixArray(s.toCharArray());
        int[] lcp = StringAlgorithm.lcpArray(s.toCharArray(), sa);
        int n = s.length();
        long ans = (long) n * (n + 1) / 2;
        for (int v : lcp) {
            ans -= v;
        }
        System.out.println(ans);
    }
}
class StringAlgorithm {
    private static int[] saNaive(int[] s) {
        int n = s.length;
        Integer[] _sa = new Integer[n];
        for (int i = 0; i < n; i++) {
            _sa[i] = i;
        }
        Arrays.sort(_sa, (l, r) -> {
            while (l < n && r < n) {
                if (s[l] != s[r]) return s[l] - s[r];
                l++;
                r++;
            }
            return -(l - r);
        });
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = _sa[i];
        }
        return sa;
    }

    private static int[] saDoubling(int[] s) {
        int n = s.length;
        Integer[] _sa = new Integer[n];
        for (int i = 0; i < n; i++) {
            _sa[i] = i;
        }
        int[] rnk = s;
        int[] tmp = new int[n];

        for (int k = 1; k < n; k *= 2) {
            final int _k = k;
            final int[] _rnk = rnk;
            Comparator<Integer> cmp = (x, y) -> {
                if (_rnk[x] != _rnk[y]) return _rnk[x] - _rnk[y];
                int rx = x + _k < n ? _rnk[x + _k] : -1;
                int ry = y + _k < n ? _rnk[y + _k] : -1;
                return rx - ry;
            };
            Arrays.sort(_sa, cmp);
            tmp[_sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[_sa[i]] = tmp[_sa[i - 1]] + (cmp.compare(_sa[i - 1], _sa[i]) < 0 ? 1 : 0);
            }
            int[] buf = tmp;
            tmp = rnk;
            rnk = buf;
        }

        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = _sa[i];
        }
        return sa;
    }

    private static final int THRESHOLD_NAIVE = 10;
    private static final int THRESHOLD_DOUBLING = 40;

    private static int[] sais(int[] s, int upper) {
        int n = s.length;
        if (n == 0) return new int[0];
        if (n == 1) return new int[]{0};
        if (n == 2) {
            if (s[0] < s[1]) {
                return new int[]{0, 1};
            } else {
                return new int[]{1, 0};
            }
        }
        if (n < THRESHOLD_NAIVE) {
            return saNaive(s);
        }
        if (n < THRESHOLD_DOUBLING) {
            return saDoubling(s);
        }

        int[] sa = new int[n];
        boolean[] ls = new boolean[n];
        for (int i = n - 2; i >= 0; i--) {
            ls[i] = s[i] == s[i + 1] ? ls[i + 1] : s[i] < s[i + 1];
        }

        int[] sumL = new int[upper + 1];
        int[] sumS = new int[upper + 1];

        for (int i = 0; i < n; i++) {
            if (ls[i]) {
                sumL[s[i] + 1]++;
            } else {
                sumS[s[i]]++;
            }
        }

        for (int i = 0; i <= upper; i++) {
            sumS[i] += sumL[i];
            if (i < upper) sumL[i + 1] += sumS[i];
        }

        Consumer<int[]> induce = lms -> {
            Arrays.fill(sa, -1);
            int[] buf = new int[upper + 1];
            System.arraycopy(sumS, 0, buf, 0, upper + 1);
            for (int d : lms) {
                if (d == n) continue;
                sa[buf[s[d]]++] = d;
            }
            System.arraycopy(sumL, 0, buf, 0, upper + 1);
            sa[buf[s[n - 1]]++] = n - 1;
            for (int i = 0; i < n; i++) {
                int v = sa[i];
                if (v >= 1 && !ls[v - 1]) {
                    sa[buf[s[v - 1]]++] = v - 1;
                }
            }
            System.arraycopy(sumL, 0, buf, 0, upper + 1);
            for (int i = n - 1; i >= 0; i--) {
                int v = sa[i];
                if (v >= 1 && ls[v - 1]) {
                    sa[--buf[s[v - 1] + 1]] = v - 1;
                }
            }
        };

        int[] lmsMap = new int[n + 1];
        Arrays.fill(lmsMap, -1);
        int m = 0;
        for (int i = 1; i < n; i++) {
            if (!ls[i - 1] && ls[i]) {
                lmsMap[i] = m++;
            }
        }

        int[] lms = new int[m];
        {
            int p = 0;
            for (int i = 1; i < n; i++) {
                if (!ls[i - 1] && ls[i]) {
                    lms[p++] = i;
                }
            }
        }

        induce.accept(lms);

        if (m > 0) {
            int[] sortedLms = new int[m];
            {
                int p = 0;
                for (int v : sa) {
                    if (lmsMap[v] != -1) {
                        sortedLms[p++] = v;
                    }
                }
            }
            int[] recS = new int[m];
            int recUpper = 0;
            recS[lmsMap[sortedLms[0]]] = 0;
            for (int i = 1; i < m; i++) {
                int l = sortedLms[i - 1], r = sortedLms[i];
                int endL = (lmsMap[l] + 1 < m) ? lms[lmsMap[l] + 1] : n;
                int endR = (lmsMap[r] + 1 < m) ? lms[lmsMap[r] + 1] : n;
                boolean same = true;
                if (endL - l != endR - r) {
                    same = false;
                } else {
                    while (l < endL && s[l] == s[r]) {
                        l++;
                        r++;
                    }
                    if (l == n || s[l] != s[r]) same = false;
                }
                if (!same) {
                    recUpper++;
                }
                recS[lmsMap[sortedLms[i]]] = recUpper;
            }

            int[] recSA = sais(recS, recUpper);

            for (int i = 0; i < m; i++) {
                sortedLms[i] = lms[recSA[i]];
            }
            induce.accept(sortedLms);
        }
        return sa;
    }

    public static int[] suffixArray(char[] s) {
        int n = s.length;
        int[] s2 = new int[n];
        for (int i = 0; i < n; i++) {
            s2[i] = s[i];
        }
        return sais(s2, 255);
    }

    public static int[] lcpArray(int[] s, int[] sa) {
        int n = s.length;
        assert (n >= 1);
        int[] rnk = new int[n];
        for (int i = 0; i < n; i++) {
            rnk[sa[i]] = i;
        }
        int[] lcp = new int[n - 1];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (h > 0) h--;
            if (rnk[i] == 0) {
                continue;
            }
            int j = sa[rnk[i] - 1];
            for (; j + h < n && i + h < n; h++) {
                if (s[j + h] != s[i + h]) break;
            }
            lcp[rnk[i] - 1] = h;
        }
        return lcp;
    }

    public static int[] lcpArray(char[] s, int[] sa) {
        int n = s.length;
        int[] s2 = new int[n];
        for (int i = 0; i < n; i++) {
            s2[i] = s[i];
        }
        return lcpArray(s2, sa);
    }
}