# Buvette Facile

Caisse enregistreuse virtuelle à partir d'une photo d'affiche de prix.

1. Prenez en photo l'affiche (ou choisissez une image).
2. Dessinez une zone sur chaque prix en glissant le doigt, saisissez le prix.
3. Enregistrez : la photo devient une caisse. Touchez une zone pour ajouter
   son prix au total, « Remise à zéro » pour le client suivant.
4. Menu ⋮ : « Modifier les prix » ou « Recommencer » avec une nouvelle photo.

## Construire l'APK

Rien à installer sur la machine à part Docker. Le build tourne dans l'image
officielle Gradle ; le SDK Android et le cache Gradle sont conservés dans deux
volumes Docker (`buvette-facile-sdk`, `buvette-facile-gradle`) :

```sh
./build.sh
```

Résultat : `out/buvette-facile.apk` (APK de debug, installable directement).
Le premier build télécharge le SDK et les dépendances (environ 1 Go, 10 à 20
minutes). Les suivants réutilisent les volumes et ne prennent que le temps de
compilation.

Le conteneur doit avoir accès à Internet. Sur une machine connectée à un VPN
d'entreprise (Cisco AnyConnect) le trafic des conteneurs est bloqué :
déconnectez le VPN le temps du build.

Pour repartir de zéro : `docker volume rm buvette-facile-sdk buvette-facile-gradle`.

## Installer sur le téléphone

Copiez `out/buvette-facile.apk` sur le téléphone (câble USB, mail, cloud),
ouvrez-le et autorisez l'installation d'applications inconnues si demandé.
Avec `adb` : `adb install -r out/buvette-facile.apk`.

## Licence

Apache License 2.0, voir `LICENSE`.
