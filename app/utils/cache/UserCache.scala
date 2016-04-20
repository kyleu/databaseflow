package utils.cache

import java.util.UUID

import models.user.User

object UserCache {
  def getUser(id: UUID) = {
    CacheService.getAs[User](s"user.$id")
  }

  def cacheUser(user: User) = {
    CacheService.set(s"user.${user.id}", user)
    for (p <- user.profiles) {
      // TODO CacheService.set(s"user.${p.providerID}:${p.providerKey}", user)
      CacheService.set(s"user.$p", user)
    }
    user
  }

  def removeUser(id: UUID) = {
    CacheService.getAs[User](s"user.$id").foreach { u =>
      for (p <- u.profiles) {
        // TODO CacheService.remove(s"user.${p.providerID}:${p.providerKey}")
        CacheService.remove(s"user.$p")
      }
    }
    CacheService.remove(s"user.$id")
    CacheService.remove(s"user.anonymous:$id")
  }
}
