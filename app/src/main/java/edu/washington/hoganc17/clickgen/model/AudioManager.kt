package edu.washington.hoganc17.clickgen.model

import android.util.Log


class AudioManager {

    fun mixAmplitudesSixteenBit(trackOne: ShortArray, trackTwo: ShortArray): ByteArray {
        // Our Code
        // 16 bit + 16 bit = 16 bit file

        // Since each value in the sampleAmplitudes is 2 bytes
        // Make output byte array the length of the longest array * 2
        val output =
            ByteArray(if (trackOne.size > trackTwo.size) trackTwo.size * 2 else trackOne.size * 2)

        var outputIndex = 0
        for (i in trackOne.indices) {
            // Numbers borrowed from Stack Overflow Code
            // Needs fine tuning as we don't really understand why the values are what they are
            val sample1 = trackOne[i] / 128.0f * 1.2f
            val sample2 = trackTwo[i] / 250.0f

            // Average the altitude of each sample into one value
            val mixed = (sample1 + sample2) / 2
            // Convert to binaryString
            var mixedString = Integer.toBinaryString(mixed.toInt())

            // Pad each value so they all have 16 bits
            mixedString = mixedString.padStart(16, '0')

            // Trim off overflow bits
            mixedString = mixedString.substring(mixedString.length - 16)

            // Separate 2 byte value into individual bytes
            val byte1 = mixedString.substring(0, 8)
            val byte2 = mixedString.substring(8, 16)

            // Add bytes sequentially to output byte array
            output[outputIndex] = Integer.parseInt(byte1, 2).toByte()
            output[outputIndex + 1] = Integer.parseInt(byte2, 2).toByte()
            outputIndex += 2
        }

        return output
    }

    fun mixAmplitudesEightBit(trackOne: ShortArray, trackTwo: ShortArray): ByteArray {
        // 8 bit file + 8 bit file = 8 bit file
        // Code from: https://stackoverflow.com/questions/40929243/how-to-mix-two-wav-files-without-noise

        val output = ByteArray(if (trackOne.size > trackTwo.size) trackTwo.size else trackTwo.size)

        for (i in output.indices) {
            val samplef1 = trackOne[i] / 128.0f * 1.2f // 2^7=128
            val samplef2 = trackTwo[i] / 128.0f * 1.2f
            val mixed = (samplef1 + samplef2) / 2
            val outputSample = (mixed * 128.0f).toByte()
            output[i] = outputSample
        }

        return output
    }

    fun generateClicktrack(times: IntArray, sr: Float, click_freq: Float, click_dur: Float, length: Int): ShortArray {
        var angular_freq: Double = 2 * Math.PI * click_freq / sr.toDouble()
        var logBins: Int = Math.round(sr * click_dur).toInt()
        var spacedVals: FloatArray = generateLogSpace(0, -10, logBins, 2.0)
        spacedVals = arange(spacedVals, angular_freq)

        var clickArrayList: ArrayList<Short> = placeClicks(spacedVals, sr, times, length)
        var clickArray: Array<Short> = clickArrayList.toTypedArray()

        return clickArray.toShortArray()
    }

    fun generateLogSpace(min: Int, max: Int, logBins: Int, logarithmicBase: Double): FloatArray {
        val delta = ((max - min) / logBins).toDouble()
        var deltaTot = 0.0
        val spacedVals = FloatArray(logBins)
        for (i in 0 until logBins) {
            spacedVals[i] = Math.pow(logarithmicBase, min + deltaTot).toFloat()
            deltaTot += delta
        }
        return spacedVals
    }

    fun arange(spacedVals: FloatArray, angular_freq: Double): FloatArray {
        for (i in 0 until spacedVals.size) {
            spacedVals[i] = spacedVals.get(i) * Math.sin(i * angular_freq).toFloat()
        }
        return spacedVals
    }

    fun timeToSamples(sr: Float, times: IntArray): IntArray {
        for (i in 0 until times.size) {
            times[i] = (times[i].toFloat() * sr).toInt()
        }
        return times
    }

    fun placeClicks(spacedVals: FloatArray, sr: Float, times: IntArray, length: Int): ArrayList<Short> {
        var positions: IntArray = timeToSamples(sr, times)
        var positionsFixed: IntArray = IntArray(positions.size)

        var c = 0
        for(i in 0 until positions.size) {
            if(positions[i] > length) {
                c--;
            } else {
                positionsFixed[c] = positions[i]
                c++;
            }
        }
        positions = positionsFixed

        var newPositions: ArrayList<Short> = arrayListOf<Short>()

        for (start in positions) {
            var end = start + spacedVals.size;
            if (end >= length) {
                //placing a click but just shortened to fit length
                for (k in 0 until length - start) {
                    newPositions.add(spacedVals[k].toShort())
                }
            } else {
                //appending clicks onto the click signal newPositions
                for (k in 0 until spacedVals.size) {
                    newPositions.add(spacedVals[k].toShort())
                }
            }
        }

        var i = 0
        while(newPositions.size < length) {
            newPositions.add(0.toShort())
            i++
        }
        Log.i("fuck RFUHDJKF", newPositions.size.toString())

        return newPositions
    }
}

