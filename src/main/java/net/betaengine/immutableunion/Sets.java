package net.betaengine.immutableunion;

public class Sets {
    private interface Set {
        Set union(Set that);

        Set incl(int elem);
    }

    private class EmptySet implements Set {
        @Override
        public Set union(final Set that) {
            return that;
        }

        @Override
        public Set incl(final int elem) {
            return new NonEmptySet(elem, new EmptySet(), new EmptySet());
        }
        
        @Override
        public String toString() {
            return ".";
        }
    }

    private class NonEmptySet implements Set {
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
            final Set leftUnion = left.union(that);
            final Set childrenUnion = right.union(leftUnion);
            final Set result = childrenUnion.incl(elem);

            return result;
        }

        @Override
        public Set incl(final int x) {
            final Set result;

            if (x < elem) {
                result = new NonEmptySet(elem, left.incl(x), right);
            } else if (x > elem) {
                result = new NonEmptySet(elem, left, right.incl(x));
            } else {
                result = this;
            }
            
            return result;
        }
        
        @Override
        public String toString() {
            return "{" + left + elem + right + "}";
        }
    }
    
    private void demo() {
        // 1 3 5 7 9 11 13
        // 2 4 6 8 10 12 14
        
        Set s1 = new EmptySet().incl(7).incl(3).incl(11).incl(1).incl(5).incl(9).incl(13);
        Set s2 = new EmptySet().incl(8).incl(4).incl(12).incl(2).incl(6).incl(10).incl(14);
        
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s1.union(s2));
    }
    
    public static void main(String[] args) {
        new Sets().demo();
    }
}
