_Notes on coding approaches to synchronizing audio with video refresh_

### On Synchronization ###
Seems that vsync has some baggage...

  * [How to enable vertical sync in opengl](http://stackoverflow.com/questions/589064/how-to-enable-vertical-sync-in-opengl)
  * [Unraveling the mystery of VSYNC](http://www.d-silence.com/feature.php?id=255)
  * [Swap Interval](http://www.opengl.org/wiki/Swap_Interval)
  * [What does "Optimal" Refresh Rate Mean?](http://www.d-silence.com/feature.php?id=206)
  * [Real Time Streaming Protocol (RTSP)](http://www.cs.columbia.edu/~hgs/rtsp/)
  * [(On the "game loop")](http://www.koonsolo.com/news/dewitters-gameloop/)
  * [Real Time Sound Event Synchronization in C++](http://ask.metafilter.com/113634/Learning-realtime-sound-event-synchronization-C-programming)

So far it seems like the best approach might be to start the sound and poll the playback system for elapsed time, adjusting animation loop to ensure coinciding events.

From OpenAL 1.1 Specification, page 39:

### Offset ###
Table 4-20.
Source AL\_SEC\_OFFSET Attribute
|**Name**|**Signature**|**Values**|**Default**|
|:-------|:------------|:---------|:----------|
|AL\_SEC\_OFFSET|f, fv, i, iv |[0.0f, any]|N/A        |


Description: the playback position, expressed in seconds (the value will loop back to zero for looping sources).

When setting AL\_SEC\_OFFSET on a source which is already playing, the playback will jump to the new offset unless the new offset is out of range, in which case an AL\_INVALID\_VALUE error is set. If the source is not playing, then the offset will be applied on the next alSourcePlay call.

The position is relative to the beginning of all the queued buffers for the source, and any queued buffers traversed by a set call will be marked as processed.

This value is based on byte position, so a pitch-shifted source will have an exaggerated playback speed. For example, you can be 0.500 seconds into a buffer having taken only 0.250 seconds to get there if the pitch is set to 2.0.


### Other ###
  * [JSyn Audio Synthesis API](http://www.softsynth.com/jsyn/)
  * [PureData RealTime A/V Programming Platform](http://puredata.info/)
  * [jMax](http://jmax.sourceforge.net/)
  * [FMOD](http://www.fmod.org/index.php/fmod)