/**
 * Copyright (c) 2009-2016 Wilfred Springer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.codehaus.preon.codec;

import org.codehaus.preon.Builder;
import org.codehaus.preon.Codec;
import org.codehaus.preon.DecodingException;
import org.codehaus.preon.Resolver;
import org.codehaus.preon.buffer.BitBuffer;
import org.codehaus.preon.channel.BitChannel;
import org.codehaus.preon.el.Expression;
import org.codehaus.preon.el.Expressions;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * The {@link org.codehaus.preon.Codec} for reading the {@link java.util.List} and its members, on demand. Instances of
 * this class will <em>not</em> create a standard {@link java.util.List} implementation and populate all of its data
 * immediately. Instead it will create a {@link org.codehaus.preon.util.EvenlyDistributedLazyList}, constructing its
 * elements on the fly, only when it is required.
 */
class ArrayCodec implements Codec<Object> {

    /** The number of elements in the list. */
    private Expression<Integer, Resolver> size;

    /** The {@link org.codehaus.preon.Codec} that will construct elements from the {@link java.util.List}. */
    private Codec<Object> codec;

    /** The type of element to be constructed. */
    private Class<?> type;

    /**
     * Constructs a new instance.
     *
     * @param expr  An {@link org.codehaus.preon.el.Expression} representing the number of elements in the {@link
     *              java.util.List}.
     * @param codec The {@link org.codehaus.preon.Codec} constructing elements in the {@link java.util.List}.
     */
    public ArrayCodec(Expression<Integer, Resolver> expr, Codec<Object> codec,
                      Class<?> type) {
        this.size = expr;
        this.codec = codec;
        this.type = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codehaus.preon.Codec#decode(org.codehaus.preon.buffer.BitBuffer,
     * org.codehaus.preon.Resolver, org.codehaus.preon.Builder)
     */

    public Object decode(BitBuffer buffer, Resolver resolver,
                         Builder builder) throws DecodingException {
        int length = size.eval(resolver).intValue();
        Object result = Array.newInstance(type.getComponentType(), length);
        for (int i = 0; i < length; i++) {
            Object value = codec.decode(buffer, resolver, builder);
            Array.set(result, i, value);
        }
        return result;
    }

    public void encode(Object object, BitChannel channel, Resolver resolver) throws IOException {
        int numberOfElements = size.eval(resolver);
        for (int i = 0; i < numberOfElements; i++) {
            codec.encode((Object) Array.get(object, i), channel, resolver);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codehaus.preon.Codec#getTypes()
     */

    public Class<?>[] getTypes() {
        return codec.getTypes();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codehaus.preon.Codec#getSize()
     */

    public Expression<Integer, Resolver> getSize() {
        return Expressions.multiply(size, codec.getSize());
    }

    public Class<?> getType() {
        return type;
    }

    public String toString() {
        return "Codec of array, decoding elements using " + codec;
    }
}
