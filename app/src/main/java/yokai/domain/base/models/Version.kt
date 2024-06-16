package yokai.domain.base.models

data class Version(
    val type: Type,
    val stage: Stage,
    val major: Int,
    val minor: Int = 0,
    val patch: Int = 0,
    val hotfix: Int = 0,
    val build: Int = 0
) {
    operator fun compareTo(other: Version): Int {
        if (type == Type.DEBUG) {
            throw IllegalStateException("Checking debug version is not allowed")
        }

        if (type != other.type) {
            throw IllegalArgumentException("Can't compare two different version type")
        }

        // On nightly we only care about build number
        if (type == Type.NIGHTLY) return build.compareTo(other.build)

        var rt = (major.compareTo(other.major) +
            minor.compareTo(other.minor) +
            patch.compareTo(other.patch) +
            hotfix.compareTo(other.hotfix)).compareTo(0)
        // if semver is equals, check version stage (release (3) > beta (2) > alpha (1))
        if (rt == 0) rt = stage.weight.compareTo(other.stage.weight)
        // if stage is also equals, we compare build number
        if (rt == 0) rt = build.compareTo(other.build)

        return rt
    }

    override fun toString(): String {
        return "$major.$minor.$patch" + (if (hotfix > 0) ".$hotfix" else "") + (if (build > 0) "-${type.prefix}$build" else "")
    }

    companion object {
        fun parse(string: String): Version {
            var type = when {
                string.startsWith("r") -> Type.NIGHTLY
                string.startsWith("d") -> Type.DEBUG
                else -> Type.STABLE
            }

            val split = string.split("-")
            var stage = Stage.RELEASE
            val stageCandidate = split.getOrNull(1)
            if (stageCandidate != null) {
                when {
                    stageCandidate.startsWith("r", true) -> type = Type.NIGHTLY
                    stageCandidate.startsWith("d", true) -> type = Type.DEBUG
                }

                if (type == Type.STABLE)
                    stage = when {
                        stageCandidate.startsWith("b", true) -> Stage.BETA
                        stageCandidate.startsWith("a", true) -> Stage.ALPHA
                        else -> Stage.RELEASE
                    }
            }
            val cleanBuild = stageCandidate
                ?.replace("[^\\d.-]".toRegex(), "")  // remove the prefix
                ?.toInt()
            val candidate = split.first()
                .replace("[^\\d.-]".toRegex(), "")  // remove the prefix
                .split(".").map { it.toInt() }

            if (candidate.size == 1 && type == Type.NIGHTLY)
                return Version(
                    type,
                    stage,
                    0,
                    0,
                    0,
                    0,
                    candidate[0],
                )

            return Version(
                type,
                stage,
                candidate.getOrNull(0) ?: 0,
                candidate.getOrNull(1) ?: 0,
                candidate.getOrNull(2) ?: 0,
                candidate.getOrNull(3) ?: 0,
                cleanBuild ?: 0,
            )
        }
    }

    enum class Stage(val weight: Int) {
        RELEASE(3),
        BETA(2),
        ALPHA(1),
    }

    enum class Type(val prefix: String) {
        STABLE("v"),
        NIGHTLY("r"),
        DEBUG("d"),
    }
}
