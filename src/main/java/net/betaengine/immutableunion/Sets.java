package net.betaengine.immutableunion;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.betaengine.algoviewer.Pusher;

public class Sets {
    public interface Set {
        Set union(Set that);

        Set incl(int elem);
        
        List<Set> getChildren();
        
        String getName();
    }
    
    public static Set createEmptySet() { return new EmptySet(); }
    
    private static class EmptySet implements Set {
        @Override
        public Set union(final Set that) {
            return push(this, () -> that, UNION_METHOD);
        }

        @Override
        public Set incl(final int elem) {
            return push(this, () -> new NonEmptySet(elem, new EmptySet(), new EmptySet()), INCL_METHOD);
        }
        
        @Override
        public List<Set> getChildren() { return ImmutableList.of(); }
        
        @Override
        public String toString() {
            return ".";
        }
        
        @Override
        public String getName() {
            return toString();
        }
    }

    private static class NonEmptySet implements Set {
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
            return push(this, () -> {
                final Set leftUnion = left.union(that);
                final Set childrenUnion = right.union(leftUnion);
                final Set result = childrenUnion.incl(elem);
    
                return result;
            }, UNION_METHOD);
        }

        @Override
        public Set incl(final int x) {
            return push(this, () -> {
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
        public List<Set> getChildren() { return ImmutableList.of(left, right); }
        
        @Override
        public String toString() {
            return "{" + left + elem + right + "}";
        }
        
        @Override
        public String getName() {
            return Integer.toString(elem);
        }
    }
    
    // -----------------------------------------------------------------
    
    private final static String UNION_METHOD = "union";
    private final static String INCL_METHOD = "incl";
    
    private static Pusher pusher;
    
    private static <T> T push(Object source, Supplier<T> s, String methodName) {
        return pusher.push(source, s, methodName);
    }
    
    public static void setPusher(Pusher pusher) {
        Sets.pusher = pusher;
    }
}
