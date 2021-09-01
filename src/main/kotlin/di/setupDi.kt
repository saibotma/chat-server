package di

import org.kodein.di.DI

fun DI.MainBuilder.setupKodein() {
    import(jacksonDi)
    import(utilDi)
    import(postgresDi)
    import(graphQlDi)
    import(pushDi)
}
