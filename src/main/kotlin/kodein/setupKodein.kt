package kodein

import org.kodein.di.DI

fun DI.MainBuilder.setupKodein() {
    import(jacksonKodein)
}
