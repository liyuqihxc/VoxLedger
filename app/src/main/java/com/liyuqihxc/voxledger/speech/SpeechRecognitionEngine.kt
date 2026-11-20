package com.liyuqihxc.voxledger.speech

/**
 * A speech-recognition backend. Implementations deliver partial and final
 * transcripts to [Listener]. All listener callbacks are invoked on the main
 * thread unless an implementation documents otherwise.
 */
interface SpeechRecognitionEngine {

    /**
     * Prepare the engine. [onReady] is invoked (possibly asynchronously) when
     * the engine is ready to record; [onError] when it is unavailable.
     */
    fun prepare(onReady: () -> Unit, onError: (String) -> Unit)

    /** Begin listening. */
    fun start(listener: Listener)

    /** Stop listening and finalize. */
    fun stop()

    /** Release resources. */
    fun destroy()

    interface Listener {
        fun onPartial(text: String)
        fun onFinal(text: String)
        fun onError(message: String)
    }
}
