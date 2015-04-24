# IMPORTANT : l'hébergeur d'images ayant eu un plantage, tous les screenshots ont disparus, ce sera fixé dans les plus "brefs" délais #



# HFR4droid : Quick start #

L'application est articulée autour de 3 types de page : la page des catégories, la page des topics, la page des posts.

## 1) La page des catégories (page principale ou racine) ##

Cet écran affiche  la liste des catégories.

![http://hfr-rehost.net/http://self/pic/c0b75f1ec05f55c33c5d9b8a14df5eb46980fd38.png](http://hfr-rehost.net/http://self/pic/c0b75f1ec05f55c33c5d9b8a14df5eb46980fd38.png)

Un double tap sur une catégorie charge et fait apparaitre les sous-catégorie de celle-ci

![http://hfr-rehost.net/http://self/pic/270d3bcbc1f5c2c11d90fd6562dd8c83504d097c.png](http://hfr-rehost.net/http://self/pic/270d3bcbc1f5c2c11d90fd6562dd8c83504d097c.png)

### Différences offline / online ###

  * _offline_ : fonctionnement simple, une pression courte permet d'afficher les topics de la catégorie.

  * _online_: Une pression courte permet d'afficher par défaut les drapeaux cyans de la catégorie. Une pression longue fait apparaitre un menu vous permettant de choisir le type de drapeaux. 2 nouvelles catégories sont présentes en haut de la liste en gras : Messages privés et Toutes les catégories, meta catégorie simulant cette page : http://forum.hardware.fr/forum1f.php?config=hfr.inc&owntopic=1

![http://hfr-rehost.net/http://self/pic/ec42c1f7fa28426a0db1b2460711d2d84163dec6.png](http://hfr-rehost.net/http://self/pic/ec42c1f7fa28426a0db1b2460711d2d84163dec6.png)

### Le menu contextuel ###

  * _Connexion / Déconnexion_ : se connecter / déconnecter d'HFR
  * _Messages privés **(uniquement en mode connecté)**_ : accéder à ses mps
  * _Paramètres_ : paramètres de l'application
  * _A propos_ : du blabla sur HFR4droid
  * _Quitter_ : quitter l'application

![http://hfr-rehost.net/http://self/pic/7bba3e61ae874bf1c475a1cb04e70816d000dc7c.png](http://hfr-rehost.net/http://self/pic/7bba3e61ae874bf1c475a1cb04e70816d000dc7c.png)

## 2) La page des topics ##

Cet écran affiche la liste des topics d'une catégorie.

![http://hfr-rehost.net/http://self/pic/5587ce685950c22dedd29b4f0e2d015e3b89b565.png](http://hfr-rehost.net/http://self/pic/5587ce685950c22dedd29b4f0e2d015e3b89b565.png) ![http://hfr-rehost.net/http://self/pic/25662d6408e48665cb0cd7e3ee1dfba56207debc.png](http://hfr-rehost.net/http://self/pic/25662d6408e48665cb0cd7e3ee1dfba56207debc.png)

### Différences offline / online ###

  * _offline_: liste simple de topics, sans drapeaux. Une pression courte permet d'afficher les posts de la page 1. Une pression longue fait apparaitre un menu vous permettant de choisir la 1ère page, une page donnée (prompt) ou la dernière page.

  * _online_ : liste de topics avec drapeaux. 4 modes : tous les topics, cyans, rouge ou favoris. Si un drapeau quelconque est présent, une pression courte permet d'afficher les posts à partir du dernier post lu (comme quand vous cliquez sur le drapeau sur HFR), si aucun drapeau n'est présent ou si il n'y a aucun nouveau message sur un topic flagué, une pression courte permet d'afficher les posts de la première page. De même, une pression longue fait apparaitre un menu vous permettant de choisir la 1ère page, une page donnée (prompt), la dernière page ainsi que la dernière page lue si un drapeau est présent. De plus, vous pouvez poster directement dans un topic sans rentrer dans celui-ci. Enfin, vous avez la possibilité de retirer le drapeau présent sur le topic.

Dans les 2 cas, si plusieurs pages sont présentes, une navigation est possible de 3 façons :
  * en touchant le bouton en haut à droite (en le faisant glisser) pour afficher le menu de navigation. (cf capture ci dessus à droite)
  * en glissant son doigt vers la droite ou la gauche (swipe)
  * en utilisant le menu (cf § suivant)

![http://hfr-rehost.net/http://self/pic/f9f45d70cac3580e703bab32b3e44c13c5d52ae4.png](http://hfr-rehost.net/http://self/pic/f9f45d70cac3580e703bab32b3e44c13c5d52ae4.png)

### Le menu contextuel ###

  * _Connexion / Déconnexion_ : se connecter / déconnecter d'HFR
  * _Mes drapeaux_ **(uniquement en mode connecté)** : recharger la page avec un type de drapeaux donné
  * _Messages privés **(uniquement en mode connecté)**_ : accèder à ses mps
  * _Paramètres_ : paramètres de l'application
  * _Rafraîchir_ : rafraîchit la page (visible uniquement dans certains cas, sinon cette option se trouve dans "Aller à...")
  * _Aller à..._ : permet de naviguer entre les pages (menu dynamique) et d'accèder aux sous catégories
  * _Retour_ : Revient à la page des catégories avec un refresh.

**/!\ NB important sur la navigation : revenir en arrière /!\**

Le comportement natif du bouton physique "back" est conservé : on revient à la fenêtre précédente sans refresh. Pour revenir à la page précédente AVEC refresh, utiliser toujours menu -> Retour

![http://hfr-rehost.net/http://self/pic/7adf13e59f174cdcc93d483df7c09afadc083e90.png](http://hfr-rehost.net/http://self/pic/7adf13e59f174cdcc93d483df7c09afadc083e90.png)

### Tip sur la navigation ###

  * Un double tap sur la barre de titre rafraichit la page courante
  * La page supporte le pull-to-refresh, tirer dessus pour rafraichir la page courante
  * Sur la page 1 le swipe à droite rafraichit également la page courante

![http://hfr-rehost.net/http://self/pic/1db0d9a3e5a005e08c4f59336d13479f8b5186ff.png](http://hfr-rehost.net/http://self/pic/1db0d9a3e5a005e08c4f59336d13479f8b5186ff.png)


### "Historique" de navigation ###

Désormais, l'historique de navigation sera conservé quand on :
  * Navigue dans les quotes
  * Fait une recherche intratopic, clique pour voir un post dans son contexte original (le _Voir ce message dans le sujet non filtré_)
- Clique sur un lien vers un autre topic depuis un topic donné (lien HFR interne)

Dans les autres cas (navigation entre pages), elle n'est pas conservée.

Exemple : Vous être sur la page 340 et cliquez sur une quote qui vous amène page 338. Dans la page 338 vous cliquez sur une quote qui vous emmène page 337. Il vous faudra faire back (bouton physique) 2 fois pour revenir page 340 et donc back 3 fois pour revenir à la page des topics. Le menu -> Retour ramènera toujours vers la page des topics.

En d'autres termes, quand vous cliquez sur une quote, vous lancez une navigation "parallèle".

Exemple bis : vous être sur la page 340 et cliquez sur une quote qui vous amène page 338. Une nouvelle navigation est enclenchée.
De la page 338 vous faites page précédente une fois, 2 fois, 3 fois et vous arrivez page 335. Si vous faites "back physique", vous revenez à la navigation principale, soit à la page 340. Idem avec la recherche et les liens internes au forum. Plusieurs niveaux de navigation peuvent être imbriqués.

### Les sous-catégories ###

Les sous-catégories sont accessible via l'option Aller à... => Une sous-catégorie du menu contextuel ou en faisant un appui long sur la barre de titre

![http://hfr-rehost.net/http://self/pic/e0e8f4e3ca996155dc85d8d92185979e9be53ae4.png](http://hfr-rehost.net/http://self/pic/e0e8f4e3ca996155dc85d8d92185979e9be53ae4.png)

## 3) La page des posts ##

Cet écran affiche la liste des posts d'un topic / mp

![http://hfr-rehost.net/http://self/pic/daf7775c708a50753a97eb4a3cc2f91e557b0b48.png](http://hfr-rehost.net/http://self/pic/daf7775c708a50753a97eb4a3cc2f91e557b0b48.png)

### Différences offline / online ###

Peux de différence, à part les limitations visuelles du mode offline : pas d'avatar, style des quotes, etc...
Dans les 2 cas, si plusieurs pages sont présentes, une navigation est possible de 3 façons (cf explication plus haut)

### Le menu contextuel ###

  * _Connexion / Déconnexion_ : se connecter / déconnecter d'HFR
  * _Messages privés **(uniquement en mode connecté)**_ : accèder à ses mps
  * _Poster un message **(uniquement en mode connecté)**_ : poster sur le topic
  * _Paramètres_ : paramètres de l'application
  * _Rafraîchir_ : rafraîchit la page (visible uniquement dans certains cas, sinon cette option se trouve dans "Aller à...")
  * _Aller à..._ : permet de naviguer entre les pages (menu dynamique)
  * _Retour_ : Revient à la page des topics avec un refresh.

![http://hfr-rehost.net/http://self/pic/6ae574e3ecba73f3e13dd6ffcd6dbfe02ffc968f.png](http://hfr-rehost.net/http://self/pic/6ae574e3ecba73f3e13dd6ffcd6dbfe02ffc968f.png)

### Tip sur la navigation ###

  * Un double tap rafraîchit la page courante
  * Sur la page 1 et la dernière page, respectivement le swipe à droite et le swipe à gauche rafraichissent la page

### Recherche de posts ###

Vous pouvez faire une recherche intratopic sur le pseudo et/ou un mot clé. Pour cela, vous devez afficher la barre de recherche. Deux possibilités :

  * Appuyer sur le bouton physique "Rechercher" de votre téléphone (très souvent une loupe)
  * Si vous n'en avez pas ou si vous préférez passer par l'écran, faites un appui long sur le titre du topic

![http://hfr-rehost.net/http://self/pic/387fa180fc200fcd2a775a99760831ece46994b8.png](http://hfr-rehost.net/http://self/pic/387fa180fc200fcd2a775a99760831ece46994b8.png)

Renseignez un pseudo et/ou un mot clé et faites ok. Les résultats s'affichent. Un appui sur retour vous repositionne sur le topic à l'endroit initial avant la recherche.

![http://hfr-rehost.net/http://self/pic/221138c2255b32cd5e958ba739464e4f298ea3ba.png](http://hfr-rehost.net/http://self/pic/221138c2255b32cd5e958ba739464e4f298ea3ba.png)

## 4) La page des messages privés ##

### Particularités ###
Cette page est identique à celle des posts mais voici les quelques différences :

  * Vous avez la possibilité de marquer un message comme "non lu" et de le supprimer en utilisant son menu contextuel
  * Vous pouvez aussi créer un nouveau message privé en utilisant l'option correspondante dans le menu contextuel de la page

![http://hfr-rehost.net/http://self/pic/31960efb7fee98562867443a05c68f3340bbe1bf.png](http://hfr-rehost.net/http://self/pic/31960efb7fee98562867443a05c68f3340bbe1bf.png)

  * Vous pouvez enfin partager des données (texte, image) par message privé, via les applications supportant l'option

![http://hfr-rehost.net/http://self/pic/e06a03fa494f82bf48cc1bbc9a45f74b40318beb.png](http://hfr-rehost.net/http://self/pic/e06a03fa494f82bf48cc1bbc9a45f74b40318beb.png)

### Les services de notification des nouveaux messages ###

Il existe 2 services indépendants, différents. Voici leur fonctionnement :

| | **Service permanent** | **Service ponctuel** |
|:|:----------------------|:---------------------|
| _Tourne en permanence_ | Oui | Non |
| _Portée_ | Totale, vous êtes notifié où que vous soyez dans votre téléphone | Locale, vous n'êtes notifié que dans HFR4droid |
| _Activé par défaut_ | Non | Oui |
| _Fait une requête sur le serveur_ | Oui | Non, sauf pour la page racine (les catégories) |
| _Type de notification_ | Barre de notification (vibre & clignote) | Barre de notification OU Toast (réglage dans les options) |
| _Type d'activation_ | Fréquentielle (par défaut toutes les heures) | Événementielle (lors de la navigation à la racine, dans une catégorie, dans un topic) |


![http://hfr-rehost.net/http://self/pic/ce1860ab4ca68129e4576537089075d6bbf6c4ce.png](http://hfr-rehost.net/http://self/pic/ce1860ab4ca68129e4576537089075d6bbf6c4ce.png)

## 5) Les préférences ##

Rien de bien sorcier à comprendre, les libellés des options sont explicites

![http://hfr-rehost.net/http://self/pic/0dd8ddbedc3a74259296da8bc85a6223fd9cc14b.png](http://hfr-rehost.net/http://self/pic/0dd8ddbedc3a74259296da8bc85a6223fd9cc14b.png)

## 6) Poster et bien plus ##

### Présentation de la fenêtre ###

Voici sa composition :

En haut, les boutons de mise en forme (Si sélection, entoure celle ci, sinon 1 clic balise de gauche, 2ème balise de droite)
Pour afficher le champs de recherche pour le wiki smilies, cliquez sur le redface. Entrez un mot clé, faites ok, choisissez votre smiley (ou faites back pour annuler la sélection)
Puis en contre bas la zone de réponse auto extensible ainsi que le bouton de validation.

![http://hfr-rehost.net/http://self/pic/b6d5d7a2f7a16235f21567c19cda2baaa924c607.png](http://hfr-rehost.net/http://self/pic/b6d5d7a2f7a16235f21567c19cda2baaa924c607.png) ![http://hfr-rehost.net/http://self/pic/a450cd95dce2fcaeaa9801e555761399a018a187.png](http://hfr-rehost.net/http://self/pic/a450cd95dce2fcaeaa9801e555761399a018a187.png)

![http://hfr-rehost.net/http://self/pic/70c752ed137be8c20e67976fe8dc8e1289a9cc7a.png](http://hfr-rehost.net/http://self/pic/70c752ed137be8c20e67976fe8dc8e1289a9cc7a.png) ![http://hfr-rehost.net/http://self/pic/60c597ca9f21e1921f5a69e80e615207a25d4bcf.png](http://hfr-rehost.net/http://self/pic/60c597ca9f21e1921f5a69e80e615207a25d4bcf.png)

#### Bouton spécial : hfr-rehost ####
Le bouton de mise en forme hfr-rehost vous demande de choisir une image sur votre mobile et l'upload automatiquement sur hfr-rehost. Ensuite le lien correspondant avec les balises IMG est inséré dans votre message.
De plus, une option hfr-rehost est désormais disponible lorsque vous voulez partager une image via les applications le supportant. Elle upload l'image et stocke son url dans le presse papier.

### Poster un message ###

Affichez simplement le menu et faites "Postez une message"

### Ouvrir la barre d'options pour un post ###

Cliquez simplement sur le header du post, n'importe ou (barre bleu, pseudo, avatar, etc...)

![http://hfr-rehost.net/http://self/pic/f9fe70d5e80247b9e2c0646071ecaa98a2a28290.png](http://hfr-rehost.net/http://self/pic/f9fe70d5e80247b9e2c0646071ecaa98a2a28290.png) ![http://hfr-rehost.net/http://self/pic/9fb7c957fc9a7ae03e338bc46bd3e64d0a4c1b3f.png](http://hfr-rehost.net/http://self/pic/9fb7c957fc9a7ae03e338bc46bd3e64d0a4c1b3f.png)

### Editer un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/thumb/http://self/pic/a9ecebcce1acea6dc274344c055b9ed4f6ff9145.png](http://hfr-rehost.net/thumb/http://self/pic/a9ecebcce1acea6dc274344c055b9ed4f6ff9145.png)
Cela va afficher la fenêtre pour poster pré-rempli avec le bbcode du message

### Supprimer un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/eab5d5e82449f988ebf35b7a310ed610026be743.png](http://hfr-rehost.net/http://self/pic/eab5d5e82449f988ebf35b7a310ed610026be743.png)
Cela va afficher une popup de confirmation, si vous validez, le post sera supprimé.

### Quoter un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/8d2ff9e5e3613095204c70c07f9113ebf07fbaab.png](http://hfr-rehost.net/http://self/pic/8d2ff9e5e3613095204c70c07f9113ebf07fbaab.png)
Cela va afficher la fenêtre pour poster pré-rempli avec la quote idoine

### Multi-quoter un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/11a3462a4cb30aff574ef2790e0ea3c940c54316.png](http://hfr-rehost.net/http://self/pic/11a3462a4cb30aff574ef2790e0ea3c940c54316.png)
Cela va ajouter la quote de façon transparente à la liste.
Pour la supprimer, réafficher la barre d'options et cliquez sur ![http://hfr-rehost.net/http://self/pic/84e0474004a3a44782c044d205d7ec2116a7b3b4.png](http://hfr-rehost.net/http://self/pic/84e0474004a3a44782c044d205d7ec2116a7b3b4.png)

Quand votre sélection est terminée, faites menu -> Poster un message et les quotes seront présentes. Cerise : le changement de page est supporté, vous pouvez faire une sélection multi pages ;)

**Important :** lorsque la quote est en train d'être ajoutée dans la pile, le bouton "Poster est message" est momentanément désactivé, et ce dans un soucis de cohérence. Il redevient activé dès lors que la quote a été ajoutée.

### Poser un favori sur un post ###

Dans la barre d'options, cliquez simplement sur ![http://hfr-rehost.net/http://self/pic/9928290c33f96f3d76fc3083a72c51c711bc3fca.png](http://hfr-rehost.net/http://self/pic/9928290c33f96f3d76fc3083a72c51c711bc3fca.png) pour poser un favori sur le post correspondant.

### Copier le lien d'un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/ab64d9920dcffd3a119708cc4565c45a29dd99ff.png](http://hfr-rehost.net/http://self/pic/ab64d9920dcffd3a119708cc4565c45a29dd99ff.png)
Cela va copier dans le presse-papier le lien http du post courant.

### Partager le lien d'un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/f1c11176b256177a19e34b59ce60551d09312f60.png](http://hfr-rehost.net/http://self/pic/f1c11176b256177a19e34b59ce60551d09312f60.png)
Un popup vous proposera une liste d'applications pour partager le lien http du post courant.

### Copier le contenu d'un message ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/4cf0e90eb8b2452c6c7e97bbc22962a20a23a60c.png](http://hfr-rehost.net/http://self/pic/4cf0e90eb8b2452c6c7e97bbc22962a20a23a60c.png)
Le contenu du message sans la mise en forme sera alors copié dans le presse-papier.

### Envoyer un message à l'auteur du post ###

Dans la barre d'options, cliquez sur ![http://hfr-rehost.net/http://self/pic/8b570b101b02e40c240486742dbf97cb99ea59d8.png](http://hfr-rehost.net/http://self/pic/8b570b101b02e40c240486742dbf97cb99ea59d8.png)
Une fenêtre vous proposera de rédiger une message privé.

### Editer les mots clés d'un smiley ###

Il suffit de cliquer sur un smiley et une fenêtre apparaitra avec une zone pour éditer les tags du dit smiley. Une fois votre modification terminée, cliquez sur OK

![http://hfr-rehost.net/http://self/pic/6f3bf3948c9feec1e18b656ab1ced77401208dba.png](http://hfr-rehost.net/http://self/pic/6f3bf3948c9feec1e18b656ab1ced77401208dba.png)

### Option sur une image d'un topic ###

Vous pouvez enregistrer, partager ou visualiser une image via un menu contextuel qui s'affiche lorsque vous faites un clic long sur une image d'un topic

![http://hfr-rehost.net/http://self/pic/866813201af3be7f64479a12d0e4ca9eadee25d8.png](http://hfr-rehost.net/http://self/pic/866813201af3be7f64479a12d0e4ca9eadee25d8.png)