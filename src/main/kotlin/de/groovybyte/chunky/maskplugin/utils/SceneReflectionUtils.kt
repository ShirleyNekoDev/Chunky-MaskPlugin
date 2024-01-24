package de.groovybyte.chunky.maskplugin.utils

import se.llbit.chunky.renderer.scene.Scene
import se.llbit.chunky.renderer.scene.SceneEntities
import se.llbit.math.bvh.BVH

val getSceneEntities = Scene::class.java
    .getSafeFieldGetterFor<Scene, SceneEntities>("entities")
val getBVH = SceneEntities::class.java
    .getSafeFieldGetterFor<SceneEntities, BVH>("bvh")
val shouldRenderActors = SceneEntities::class.java
    .getSafeFieldGetterFor<SceneEntities, Boolean>("renderActors")
val getActorBVH = SceneEntities::class.java
    .getSafeFieldGetterFor<SceneEntities, BVH>("actorBvh")
