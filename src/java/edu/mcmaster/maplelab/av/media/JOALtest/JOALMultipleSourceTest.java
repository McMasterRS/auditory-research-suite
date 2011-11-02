
package edu.mcmaster.maplelab.av.media.JOALtest;
/**
* Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* -Redistribution of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer.
*
* -Redistribution in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* Neither the name of Sun Microsystems, Inc. or the names of contributors may 
* be used to endorse or promote products derived from this software without 
* specific prior written permission.
* 
* This software is provided "AS IS," without a warranty of any kind.
* ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
* ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
* NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS
* LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
* RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
* IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
* OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
* PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
* ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
* BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
*
* You acknowledge that this software is not designed or intended for use in the
* design, construction, operation or maintenance of any nuclear facility.
*
* Created on Jun 24, 2003
* 
* see http://www.devmaster.net/articles/openal-tutorials/lesson3.php
* 
* modified by bguseman
*/

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;


public class JOALMultipleSourceTest {

    static AL al;
    static final int NUM_BUFFERS = 3;
    static final int NUM_SOURCES = 3;

    static final int BATTLE = 0;
    static final int GUN1 = 1;
    static final int GUN2 = 2;

    static IntBuffer buffers = IntBuffer.allocate(NUM_BUFFERS);
    static IntBuffer sources = IntBuffer.allocate(NUM_SOURCES);

    static FloatBuffer[] sourcePos = new FloatBuffer[NUM_SOURCES];
    static FloatBuffer[] sourceVel = new FloatBuffer[NUM_SOURCES];
    static FloatBuffer listenerPos = FloatBuffer.wrap(new float[]{ 0.0f, 0.0f, 0.0f });
    static FloatBuffer listenerVel = FloatBuffer.wrap(new float[]{ 0.0f, 0.0f, 0.0f });
    static FloatBuffer listenerOri = FloatBuffer.wrap(new float[]{ 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f });

    static int loadALData() {
        //variables to load into
        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];
        
        // load wav data into buffers
        al.alGenBuffers(NUM_BUFFERS, buffers);
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        ALut.alutLoadWAVFile(JOALMultipleSourceTest.class.getResourceAsStream("wavdata/Battle.wav")
            /*"wavdata/Battle.wav"*/,
            format,
            data,
            size,
            freq,
            loop);
        al.alBufferData(
            buffers.get(BATTLE),
            format[0],
            data[0],
            size[0],
            freq[0]);

        ALut.alutLoadWAVFile(JOALMultipleSourceTest.class.getResourceAsStream("wavdata/Gun1.wav")
            /*"wavdata/Gun1.wav"*/,
            format,
            data,
            size,
            freq,
            loop);
        al.alBufferData(
            buffers.get(GUN1),
            format[0],
            data[0],
            size[0],
            freq[0]);

        ALut.alutLoadWAVFile(JOALMultipleSourceTest.class.getResourceAsStream("wavdata/Gun2.wav")
            /*"wavdata/Gun2.wav"*/,
            format,
            data,
            size,
            freq,
            loop);
        al.alBufferData(
            buffers.get(GUN2),
            format[0],
            data[0],
            size[0],
            freq[0]);
        
        for (int i = 0; i < NUM_SOURCES; i++) {
        	sourcePos[i] = FloatBuffer.allocate(3);
            sourceVel[i] = FloatBuffer.allocate(3);
        }

        // bind buffers into audio sources
        al.alGenSources(NUM_SOURCES, sources);

        al.alSourcei(sources.get(BATTLE), AL.AL_BUFFER, buffers.get(BATTLE));
        al.alSourcef(sources.get(BATTLE), AL.AL_PITCH, 1.0f);
        al.alSourcef(sources.get(BATTLE), AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources.get(BATTLE), AL.AL_POSITION, sourcePos[BATTLE]);
        al.alSourcefv(sources.get(BATTLE), AL.AL_POSITION, sourceVel[BATTLE]);
        al.alSourcei(sources.get(BATTLE), AL.AL_LOOPING, AL.AL_TRUE);

        al.alSourcei(sources.get(GUN1), AL.AL_BUFFER, buffers.get(GUN1));
        al.alSourcef(sources.get(GUN1), AL.AL_PITCH, 1.0f);
        al.alSourcef(sources.get(GUN1), AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources.get(GUN1), AL.AL_POSITION, sourcePos[GUN1]);
        al.alSourcefv(sources.get(GUN1), AL.AL_POSITION, sourceVel[GUN1]);
        al.alSourcei(sources.get(GUN1), AL.AL_LOOPING, AL.AL_FALSE);

        al.alSourcei(sources.get(GUN2), AL.AL_BUFFER, buffers.get(GUN2));
        al.alSourcef(sources.get(GUN2), AL.AL_PITCH, 1.0f);
        al.alSourcef(sources.get(GUN2), AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources.get(GUN2), AL.AL_POSITION, sourcePos[GUN2]);
        al.alSourcefv(sources.get(GUN2), AL.AL_POSITION, sourceVel[GUN2]);
        al.alSourcei(sources.get(GUN2), AL.AL_LOOPING, AL.AL_FALSE);

        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        return AL.AL_TRUE;
    }

    static void setListenerValues() {
        al.alListenerfv(AL.AL_POSITION, listenerPos);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri);
    }

    static void killAllData() {
        al.alDeleteBuffers(NUM_BUFFERS, buffers);
        al.alDeleteSources(NUM_SOURCES, sources);
        ALut.alutExit();
    }

    public static void main(String[] args) {
        ALut.alutInit();
        al = ALFactory.getAL();
        al.alGetError();
        
        if(loadALData() == AL.AL_FALSE) {
            System.exit(1);    
        }
        setListenerValues();
        al.alSourcePlay(sources.get(BATTLE));
        long startTime = System.currentTimeMillis();
        long elapsed = 0;
        long totalElapsed = 0;
        Random rand = new Random();
        IntBuffer state = IntBuffer.allocate(1);
        while (totalElapsed < 10000) {
            elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 50) {
                totalElapsed += elapsed;
                startTime = System.currentTimeMillis();
                // pick one of the two guns
                int pick = Math.abs((rand.nextInt()) % 2) + 1;
                al.alGetSourcei(sources.get(pick), AL.AL_SOURCE_STATE, state);
                if (state.get(0) != AL.AL_PLAYING) {
                    double theta = (rand.nextInt() % 360) * 3.14 / 180.0;
                    sourcePos[pick].put(0, - ((float) Math.cos(theta)));
                    sourcePos[pick].put(1, - ((float) (rand.nextInt() % 2)));
                    sourcePos[pick].put(2, - ((float) Math.sin(theta)));

                    al.alSourcefv(
                    	sources.get(pick),
                        AL.AL_POSITION,
                        sourcePos[pick]);

                    al.alSourcePlay(sources.get(pick));
                }
            }
        }
        killAllData();
    }
}
