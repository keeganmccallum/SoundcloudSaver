package com.keeganmccallum.soundcloudsaver;

import java.util.ArrayList;

/**
 * Created by keeganmccallum on 10/11/14.
 */
public class CopySCDB extends ExecuteAsRootBase{
    @Override
    protected ArrayList<String> getCommandsToExecute() {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("cp /data/data/com.soundcloud.android/databases/SoundCloud /sdcard/");
        return commands;
    }
}
