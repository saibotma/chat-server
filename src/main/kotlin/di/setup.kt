package di

import org.kodein.di.DI

fun DI.MainBuilder.setupDi() {
    import(jacksonDi)
    import(utilDi)
    import(postgresDi)
    import(graphQlDi)
    import(pushDi)
}
