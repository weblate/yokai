package yokai.domain.extension.repo.exception

import java.io.IOException

class FetchExtensionRepoException(throwable: Throwable) :
    IOException("Failed to retrieve Extension Repo Details", throwable)
