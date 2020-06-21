package com.yibasan.opengl30demo.util

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer

class VideoEncoderCode(widht: Int, height: Int, bitRate: Int, outputFile: File) {
    companion object {
        private val TAG = "VideoEncoderCode"

        private val VERBOSE = false

        private val MEME_TYPE = "video/avc"
        private val FRAME_RATE = 30
        private val IFRAME_INTERVAL = 5

    }


    private var mInputSurface: Surface? = null
    private var mMuxer: MediaMuxer? = null
    private var mEncoder: MediaCodec? = null
    private var mBufferInfo: MediaCodec.BufferInfo? = null

    private var mTrackIndex: Int = 0
    private var mMuxerStarted: Boolean = false

    init {

        mBufferInfo = MediaCodec.BufferInfo()

        var format: MediaFormat = MediaFormat.createVideoFormat(MEME_TYPE, widht, height)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)
        Log.d(
            TAG,
            "bitRate: $bitRate, frameRate:$FRAME_RATE,IframeInterval:$IFRAME_INTERVAL,width:$widht,height:$height"
        )
        mEncoder = MediaCodec.createEncoderByType(MEME_TYPE)
        mEncoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = mEncoder?.createInputSurface()
        mEncoder?.start()

        mMuxer = MediaMuxer(outputFile.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        mTrackIndex = -1
        mMuxerStarted = false
    }


    fun getInputSurface(): Surface? {
        return mInputSurface
    }

    fun release() {
        if (mEncoder != null) {
            mEncoder?.stop()
            mEncoder?.release()
            mEncoder = null
        }

        if (mMuxer != null) {
            mMuxer?.stop()
            mMuxer?.release()
            mMuxer = null
        }
    }


    fun drainEncoder(endOfStream: Boolean) {

        val TIMOUT_USEC = 10000
        if (endOfStream) {
            mEncoder?.signalEndOfInputStream()
        }

        var encoderOutputBuffers = mEncoder?.outputBuffers
        while (true) {
            var encoderStatus = mEncoder?.dequeueOutputBuffer(mBufferInfo!!, TIMOUT_USEC.toLong())
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break
                } else {
                    Log.e(TAG, "no output available,spinning to await EOS")
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mEncoder?.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw RuntimeException("format changed twice")
                }
                var newFormat: MediaFormat? = mEncoder?.outputFormat
                Log.d(
                    TAG,
                    "encoder output format changed: $newFormat"
                )
                newFormat?.let {
                    mTrackIndex = mMuxer?.addTrack(newFormat)!!
                }
                mMuxer?.start()
                mMuxerStarted = true
            } else if (encoderStatus!! < 0) {
                Log.w(
                    TAG,
                    "unexpected result from encoder.dequeueOutputBuffer: " +
                            encoderStatus
                )
            } else {
                var encodedData: ByteBuffer? = encoderOutputBuffers!![encoderStatus!!]
                    ?: throw RuntimeException(
                        "encoderOutputBuffer " + encoderStatus +
                                " was null"
                    )

                if (mBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    mBufferInfo!!.size = 0
                }
                if (mBufferInfo!!.size != 0) {
                    if (!mMuxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }

                    encodedData?.position(mBufferInfo!!.offset)
                    encodedData?.limit(mBufferInfo!!.offset + mBufferInfo!!.size)

                    mMuxer?.writeSampleData(mTrackIndex, encodedData!!, mBufferInfo!!)

                    if ((mBufferInfo?.flags!! and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (!endOfStream) {
                            Log.w(
                                TAG,
                                "reached end of stream unexpectedly"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "end of stream reached"
                            )
                        }
                        break
                    }
                }
            }

        }
    }

}