import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StageDecorationsTest {
    @Nested
    class WithoutGroup {
        @Test
        void applyEmptyDecorationsShouldRunInnerClosure() {
            def testSpy = spy(new TestSpy())
            def decorations = new StageDecorations()

            decorations.apply {
                testSpy.foo()
            }

            verify(testSpy, times(1)).foo()
        }

        @Test
        void applyRunsSingleDecoration() {
            def testSpy = spy(new TestSpy())
            def decorations = new StageDecorations()

            decorations.add { nestedClosure -> testSpy.foo() }
            decorations.apply { }

            verify(testSpy, times(1)).foo()
        }

        @Test
        void applyRunsMultipleNestedClosures() {
            def testSpy = spy(new TestSpy())

            def outerClosure = { nestedClosure -> nestedClosure(); testSpy.outerClosureCalled() }
            def middleClosure = { nestedClosure -> nestedClosure(); testSpy.middleClosureCalled() }
            def innerClosure = { nestedClosure -> nestedClosure(); testSpy.innerClosureCalled() }

            def decorations = new StageDecorations()
            decorations.add(innerClosure)
            decorations.add(middleClosure)
            decorations.add(outerClosure)

            decorations.apply {
                testSpy.inlinedClosureCalled()
            }

            verify(testSpy, times(1)).outerClosureCalled()
            verify(testSpy, times(1)).middleClosureCalled()
            verify(testSpy, times(1)).innerClosureCalled()
            verify(testSpy, times(1)).inlinedClosureCalled()
        }

        private class TestSpy {
            public foo() { return }
            public inlinedClosureCalled() { return }
            public innerClosureCalled() { return }
            public middleClosureCalled() { return }
            public outerClosureCalled() { return }
        }
    }

    @Nested
    class WithGroup {
        @Test
        void applyEmptyDecorationsShouldRunInnerClosure() {
            def testSpy = spy(new TestSpy())
            def decorations = new StageDecorations()

            decorations.apply('group1') {
                testSpy.foo()
            }

            verify(testSpy, times(1)).foo()
        }

        @Test
        void applyRunsAddedDecoration() {
            def testSpy = spy(new TestSpy())
            def group = 'somegroup'
            def decorations = new StageDecorations()

            decorations.add(group) { testSpy.foo() }
            decorations.apply(group) {
            }

            verify(testSpy, times(1)).foo()
        }

        @Test
        void applyHandlesMultipleNestedClosures() {
            def testSpy = spy(new TestSpy())
            def group = 'somegroup'

            def outerClosure = { nestedClosure -> nestedClosure(); testSpy.outerClosureCalled() }
            def middleClosure = { nestedClosure -> nestedClosure(); testSpy.middleClosureCalled() }
            def innerClosure = { nestedClosure -> nestedClosure(); testSpy.innerClosureCalled() }

            def decorations = new StageDecorations()
            decorations.add(group, innerClosure)
            decorations.add(group, middleClosure)
            decorations.add(group, outerClosure)

            decorations.apply(group) {
                testSpy.inlinedClosureCalled()
            }

            verify(testSpy, times(1)).outerClosureCalled()
            verify(testSpy, times(1)).middleClosureCalled()
            verify(testSpy, times(1)).innerClosureCalled()
            verify(testSpy, times(1)).inlinedClosureCalled()
        }

        private class TestSpy {
            public foo() { return }
            public inlinedClosureCalled() { return }
            public innerClosureCalled() { return }
            public middleClosureCalled() { return }
            public outerClosureCalled() { return }
        }
    }

    @Nested
    class WithAndWithoutGroups {
        @Test
        void applyWithoutGroupDoesNotRunGroupDecorations() {
            def withoutGroup = spy(new TestSpy())
            def withGroup = spy(new TestSpy())
            def group = 'somegroup'

            def closureWithoutGroup = { innerClosure -> withoutGroup.foo() }
            def closureWithGroup = { innerClosure -> withGroup.foo() }

            def decorations = new StageDecorations()
            decorations.add(closureWithoutGroup)
            decorations.add(group, closureWithGroup)

            decorations.apply { -> }

            verify(withoutGroup, times(1)).foo()
            verify(withGroup, times(0)).foo()
        }

        @Test
        void applyWithGroupDoesNotRunDecorationsWithoutGroups() {
            def withoutGroup = spy(new TestSpy())
            def withGroup = spy(new TestSpy())
            def group = 'somegroup'

            def closureWithoutGroup = { innerClosure -> withoutGroup.foo() }
            def closureWithGroup = { innerClosure -> withGroup.foo() }

            def decorations = new StageDecorations()
            decorations.add(closureWithoutGroup)
            decorations.add(group, closureWithGroup)

            decorations.apply(group) { -> }

            verify(withoutGroup, times(0)).foo()
            verify(withGroup, times(1)).foo()
        }

        private class TestSpy {
            public foo() { return }
        }
    }
}
