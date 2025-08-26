package me.theentropyshard.crlauncher.cosmic.mods.puzzle;

import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.QuiltMavenArtifact;

import java.util.List;

public record PuzzleUnknownRepoDependency(List<String> baseReposURL, QuiltMavenArtifact mavenArtifact) {

}
