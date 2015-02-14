package com.github.ompc.echo.server.framework.buffer;

import java.nio.ByteBuffer;

import static java.lang.System.arraycopy;

/**
 * 动态扩容ByteBuffer
 * Created by vlinux on 15/2/14.
 */
public class DynamicByteBuffer {

    private byte[] data;

    private int readerIndex = 0;
    private int writerIndex = 0;

    public DynamicByteBuffer(int bSize) {
        data = new byte[bSize];
    }

    public void write(ByteBuffer byteBuffer) {

        ensureWritableBytes(byteBuffer.remaining());
        while (byteBuffer.hasRemaining()) {
            data[writerIndex++] = byteBuffer.get();
        }

    }

    public byte read() {
        if (isReaderLimit()) {
            throw new IndexOutOfBoundsException("Readable byte limit exceeded:" + readerIndex);
        }
        return data[readerIndex++];
    }

    public int read(byte[] dst) {
        final int length = dst.length >= readableBytes() ? readableBytes() : dst.length;
        arraycopy(data, readerIndex, dst, 0, readableBytes());
        readerIndex += length;
        return length;
    }

    /**
     * 可读字节数
     *
     * @return
     */
    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    /**
     * 动态扩容，若请求写的容量不够，则让Buffer自动扩容
     *
     * @param wSize 本次需要扩容字节数
     */
    private void ensureWritableBytes(int wSize) {

        if (isWriterLimit()) {
            final int length = (data.length * 2) >= (data.length + wSize) ? (data.length * 2) : (data.length + wSize);
            final byte[] new_data = new byte[length];
            arraycopy(data, 0, new_data, 0, data.length);
            data = new_data;
        }

    }

    /**
     * 是否到达读上限
     *
     * @return
     */
    private boolean isReaderLimit() {
        return readerIndex == writerIndex;
    }

    /**
     * 是否到达写上限
     *
     * @return
     */
    private boolean isWriterLimit() {
        return writerIndex == data.length;
    }

}
