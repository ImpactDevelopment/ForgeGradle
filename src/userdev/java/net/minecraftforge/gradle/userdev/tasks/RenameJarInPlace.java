/*
 * ForgeGradle
 * Copyright (C) 2018 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradle.userdev.tasks;

import net.minecraftforge.gradle.common.task.JarExec;
import net.minecraftforge.gradle.common.util.MappingFile;
import net.minecraftforge.gradle.common.util.Utils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RenameJarInPlace extends JarExec {
    private Supplier<File> input;
    private File temp;
    private Supplier<File> mappings;

    private String jarTask;
    private MappingFile.Mapping mappingType;
    private final List<File> extraSrgs = new ArrayList<>();

    public RenameJarInPlace() {
        tool = Utils.SPECIALSOURCE;
        args = new String[] { "--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}", "--live"};
        this.getOutputs().upToDateWhen(task -> false);
    }

    @Override
    protected List<String> filterArgs() {

        Map<String, String> replace = new HashMap<>();
        replace.put("{input}", getInput().getAbsolutePath());
        replace.put("{output}", temp.getAbsolutePath());
        replace.put("{mappings}", getMappings().getAbsolutePath());

        List<String> copy = new ArrayList<>(Arrays.asList(getArgs()));

        // Inject our extra SRGs
        extraSrgs.forEach(srg -> {
            copy.add("--srg-in");
            copy.add(srg.getAbsolutePath());
        });

        setArgs(copy);

        return Arrays.stream(getArgs()).map(arg -> replace.getOrDefault(arg, arg)).collect(Collectors.toList());
    }

    @Override
    @TaskAction
    public void apply() throws IOException {
        temp = getProject().file("build/" + getName() + "/output.jar");
        if (!temp.getParentFile().exists())
            temp.getParentFile().mkdirs();

        super.apply();

        FileUtils.copyFile(temp, getInput());
    }

    @InputFile
    public File getMappings() {
        return mappings.get();
    }
    public void setMappings(File value) {
        this.mappings = () -> value;
    }
    public void setMappings(Supplier<File> value) {
        this.mappings = value;
    }

    @InputFile
    public File getInput() {
        return input.get();
    }
    public void setInput(File value) {
        this.input = () -> value;
    }
    public void setInput(Supplier<File> value) {
        this.input = value;
    }

    @Input
    public MappingFile.Mapping getMappingType() {
        return mappingType;
    }
    public void setMappingType(MappingFile.Mapping mappingType) {
        this.mappingType = mappingType;
    }

    @Input
    public String getJarTask() {
        return jarTask;
    }
    public void setJarTask(String jarTask) {
        this.jarTask = jarTask;
    }

    public void extraFiles(File... srgs) {
        extraSrgs.addAll(Arrays.asList(srgs));
    }
}
