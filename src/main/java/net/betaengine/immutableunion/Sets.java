package net.betaengine.immutableunion;

import java.util.function.Supplier;

public class Sets {
    private final CallStack stack = new CallStack();
    
    private interface Set {
        Set union(Set that);

        Set incl(int elem);
    }
    
    private abstract class AbstractSet implements Set {
        protected final static String UNION_METHOD = "union";
        protected final static String INCL_METHOD = "incl";
        
        protected <T> T push(Supplier<T> s, String methodName) {
            try {
                stack.push(this, methodName);
                return s.get();
            } finally {
                stack.pop();
            }
        }
    }

    private class EmptySet extends AbstractSet {
        @Override
        public Set union(final Set that) {
            return push(() -> that, UNION_METHOD);
        }

        @Override
        public Set incl(final int elem) {
            return push(() -> new NonEmptySet(elem, new EmptySet(), new EmptySet()), INCL_METHOD);
        }
        
        @Override
        public String toString() {
            return ".";
        }
    }

    private class NonEmptySet extends AbstractSet {
        private final int elem;
        private final Set left;
        private final Set right;

        // TODO: should be package private so only NonEmptySet and EmptySet can access it.
        public NonEmptySet(final int elem, final Set left, final Set right) {
            this.elem = elem;
            this.left = left;
            this.right = right;
        }

        @Override
        public Set union(final Set that) {
            return push(() -> {
                final Set leftUnion = left.union(that);
                final Set childrenUnion = right.union(leftUnion);
                final Set result = childrenUnion.incl(elem);
    
                return result;
            }, UNION_METHOD);
        }

        @Override
        public Set incl(final int x) {
            return push(() -> {
                final Set result;
    
                if (x < elem) {
                    result = new NonEmptySet(elem, left.incl(x), right);
                } else if (x > elem) {
                    result = new NonEmptySet(elem, left, right.incl(x));
                } else {
                    result = this;
                }
                
                return result;
            }, INCL_METHOD);
        }
        
        @Override
        public String toString() {
            return "{" + left + elem + right + "}";
        }
    }
    
    private void demo() {
        Set s1 = new EmptySet().incl(7).incl(3).incl(11).incl(1).incl(5).incl(9).incl(13);
        Set s2 = new EmptySet().incl(8).incl(4).incl(12).incl(2).incl(6).incl(10).incl(14);
        
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s1.union(s2));
        System.out.println(stack);
    }
    
    public static void main(String[] args) {
        new Sets().demo();
    }
}
