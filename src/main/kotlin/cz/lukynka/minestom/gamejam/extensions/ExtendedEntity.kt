package cz.lukynka.minestom.gamejam.extensions

import net.minestom.server.entity.Entity

val Entity.location get() = this.position.toLocation(this.instance)