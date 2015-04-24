# Changelog #

## 0.8.6 ##
### Bugs fixés ###
  * Correction du bug sur les profils (changement du html)
  * Correction du bug sur le freeze des pages (merci Ayuget)

---

## 0.8.5 ##
### Bugs fixés ###
  * Correction du bug sur le chargement des pages
  * Passage de hfr-rehost.net à reho.st
  * Curseur absent sur le champ de recherche de smileys [issue #142](https://code.google.com/p/hfr4droid/issues/detail?id=#142)
  * Correction de bugs divers

---

## 0.8.4 ##
### Bugs fixés ###
  * Refonte de l'affichage des boutons / zones de saisie pour tous les thèmes
  * Le titre d'un topic ne défilait plus sur Android >= 4.x

---

## 0.8.3 ##
_(Pas de 0.8.2, version corrompue non valide)_
### Bugs fixés ###
  * Textes noirs sur fond gris avec le thème Dark
  * Bug sur le curseur absent dans la fenêtre de saisie d'un post
  * Bug sur le multiquote quand le chargement des quotes n'était pas terminé
  * Force close lors du changement d'orientation pendant un chargement sur Android >= 3.x

---

## 0.8.1 ##
### Bugs fixés ###
  * Textes blancs sur fond blanc sur Android >= 4.x
  * Crash sur la page d'un topic en mode déconnecté

---

## 0.8.0 ##
### Ajouts ###
  * Emplacement du bandeau "cité x fois" dans un post [issue #85](https://code.google.com/p/hfr4droid/issues/detail?id=#85)
  * Vérification de la taille et resize automatique des images pour Hfr-Rehost avant upload [issue #112](https://code.google.com/p/hfr4droid/issues/detail?id=#112)
  * Support du choix du multi pour le préchargement des pages, afin de gérer un nombre personnalisé de posts par page [issue #117](https://code.google.com/p/hfr4droid/issues/detail?id=#117)
  * Ajout de raccourcis pour aller directement en haut ou bas de page lors de la lecture d'un topic [issue #119](https://code.google.com/p/hfr4droid/issues/detail?id=#119)
  * Ajout de l'Action Bar [issue #127](https://code.google.com/p/hfr4droid/issues/detail?id=#127)
  * Support de l'alerte modérateur et changement des icônes pour stopper la confusion avec l'alerte qualitaÿ [issue #128](https://code.google.com/p/hfr4droid/issues/detail?id=#128)

### Bugs fixés ###
  * ICS, bug annulation de l'ouverture d'un topic [issue #63](https://code.google.com/p/hfr4droid/issues/detail?id=#63)
  * La lecture des sujets désactive la notif par email [issue #118](https://code.google.com/p/hfr4droid/issues/detail?id=#118)
  * Affichage incorrect réponse effacée pour modo [issue #120](https://code.google.com/p/hfr4droid/issues/detail?id=#120)
  * Appui sur "Favoris" dans les MP [issue #123](https://code.google.com/p/hfr4droid/issues/detail?id=#123)
  * Plantage application lors du chargement d'un topic particulier linké dans un autre [issue #125](https://code.google.com/p/hfr4droid/issues/detail?id=#125)
  * Recherche intra-topic - Syntaxe, priez pour nous [issue #126](https://code.google.com/p/hfr4droid/issues/detail?id=#126)

---

## 0.7.4 ##
_(Pas de 0.7.3, oubli d'un bug donc passage direct en 0.7.4)_
### Bugs fixés ###
  * Encodage dans fenêtre "à propos"  [issue #114](https://code.google.com/p/hfr4droid/issues/detail?id=#114)
  * Possibilité de se logger (sans vraiment l'être) avec un login / mdp invalide  [issue #115](https://code.google.com/p/hfr4droid/issues/detail?id=#115)

---

## 0.7.2 ##
### Ajouts ###
  * Ajouter la compatibilité multi-fenêtres  [issue #109](https://code.google.com/p/hfr4droid/issues/detail?id=#109)
  * Demande de confirmation avant d'upload sur HFR Rehost [issue #110](https://code.google.com/p/hfr4droid/issues/detail?id=#110)

### Bugs fixés ###
  * Edition de message impossible si posté en avant-dernière page [issue #107](https://code.google.com/p/hfr4droid/issues/detail?id=#107)
  * Images HTTPS non filtrés [issue #111](https://code.google.com/p/hfr4droid/issues/detail?id=#111)
  * Rédaction message via appuis long sur un topik - Taille des caractères différent de la config [issue #113](https://code.google.com/p/hfr4droid/issues/detail?id=#113)
  * Correction de bugs qui provoquaient des force close (remontés via rapport)

---

## 0.7.1 ##
### Ajouts ###
  * Ajout de détails supplémentaires dans la liste des topics (désactivé par défaut) [issue #89](https://code.google.com/p/hfr4droid/issues/detail?id=#89)
  * Amélioration de la navigation intra-topic

### Bugs fixés ###
  * Force close, lors de l'utilisation de la fonction de partage "Hfr4droid, envoyer par MP"   [issue #103](https://code.google.com/p/hfr4droid/issues/detail?id=#103)
  * Force close, lors d'un "clic trackball" sur le raccourcis "Appuyer pour rafraîchir ..." [issue #104](https://code.google.com/p/hfr4droid/issues/detail?id=#104)
  * Impossible de cliquer sur l'une des catégories (Hardware, Ordinateurs portables) à l'aide du trackball.  [issue #105](https://code.google.com/p/hfr4droid/issues/detail?id=#105)
  * Correction de bugs qui provoquaient des force close (remontés via rapport)

---

## 0.7.0 ##
### Ajouts ###
  * Intégration du profil utilisateur [issue #3](https://code.google.com/p/hfr4droid/issues/detail?id=#3)
  * Nouvelle navigation intuitive entre les pages avec préchargement automatique [issue #83](https://code.google.com/p/hfr4droid/issues/detail?id=#83)
  * Possibilité de lancer une Alerte Qualitaÿ sur un post donné [issue #79](https://code.google.com/p/hfr4droid/issues/detail?id=#79)
  * Optimisations de l'application (mémoire, consommation, accélération matérielle, rapidité globale) [issue #62](https://code.google.com/p/hfr4droid/issues/detail?id=#62) [issue #65](https://code.google.com/p/hfr4droid/issues/detail?id=#65) [issue #81](https://code.google.com/p/hfr4droid/issues/detail?id=#81)
  * Affichage des informations EXIF des images [issue #51](https://code.google.com/p/hfr4droid/issues/detail?id=#51)
  * Features en vrac : Ajout d'une taille de police, bouton réessayer en cas d'erreur de connexion [issue #98](https://code.google.com/p/hfr4droid/issues/detail?id=#98), [issue #78](https://code.google.com/p/hfr4droid/issues/detail?id=#78)

### Bugs fixés ###
  * Popup persistant sur double tap  [issue #37](https://code.google.com/p/hfr4droid/issues/detail?id=#37)
  * Certaines urls hfr sont mals mappées [issue #66](https://code.google.com/p/hfr4droid/issues/detail?id=#66)
  * Comportement du bouton retour dans les MPs [issue #68](https://code.google.com/p/hfr4droid/issues/detail?id=#68)
  * Impossible d'annuler le chargement durant le spash screen [issue #69](https://code.google.com/p/hfr4droid/issues/detail?id=#69)
  * Douple tap sous cat [issue #73](https://code.google.com/p/hfr4droid/issues/detail?id=#73)
  * Ouverture lien topic mort [issue #74](https://code.google.com/p/hfr4droid/issues/detail?id=#74)
  * Toast vide en cat 0 [issue #97](https://code.google.com/p/hfr4droid/issues/detail?id=#97)

---

## 0.6.1 ##
### Bugs fixés ###
  * Pas de retour aux catégories quand on a plus de drapeaux à afficher [issue #60](https://code.google.com/p/hfr4droid/issues/detail?id=#60)
  * Annulation de chargement d'une catégorie [issue #61](https://code.google.com/p/hfr4droid/issues/detail?id=#61)

---

## 0.6.0 ##
### Ajouts ###
  * Ajout de la recherche intratopic avec un mot clé et/ou un pseudo [issue #46](https://code.google.com/p/hfr4droid/issues/detail?id=#46)
  * Implémentation du pull-to-refresh sur la page des topics [issue #47](https://code.google.com/p/hfr4droid/issues/detail?id=#47)
  * Ajout d'options dans la quick actions bar du post : copier / coller du contenu et envoi d'un MP à l'auteur [issue #5](https://code.google.com/p/hfr4droid/issues/detail?id=#5) [issue #23](https://code.google.com/p/hfr4droid/issues/detail?id=#23)
  * Possibilité de supprimer un MP [issue #52](https://code.google.com/p/hfr4droid/issues/detail?id=#52)
  * Conservation de la navigation lors d’un clic sur une quote, lien HFR interne ou post issu d'une recherche [issue #50](https://code.google.com/p/hfr4droid/issues/detail?id=#50)
  * Nouveau splash screen
  * Features en vrac : numéro de page max  dans "Aller à la page...", confirmation à la déconnexion, barre grise de fin de topic toujours en bas, sauvegarde d'une image dans son format original, affichage du titre d'un topic avec le prefix entre crochets à la fin [issue #24](https://code.google.com/p/hfr4droid/issues/detail?id=#24) [issue #36](https://code.google.com/p/hfr4droid/issues/detail?id=#36) [issue #54](https://code.google.com/p/hfr4droid/issues/detail?id=#54) [issue #55](https://code.google.com/p/hfr4droid/issues/detail?id=#55) [issue #27](https://code.google.com/p/hfr4droid/issues/detail?id=#27)

### Bugs fixés ###
  * Posts de la Modération illisibles avec le thème Dark  [issue #31](https://code.google.com/p/hfr4droid/issues/detail?id=#31)
  * Pas de balise quote [issue #35](https://code.google.com/p/hfr4droid/issues/detail?id=#35)
  * Expiration des cookies [issue #38](https://code.google.com/p/hfr4droid/issues/detail?id=#38)
  * Upload sur hfr-rehost defectueux [issue #39](https://code.google.com/p/hfr4droid/issues/detail?id=#39)

---

## 0.5.1 ##
### Bugs fixés ###
  * Correction d'un bug dans le menu contextuel de certaines images [issue #25](https://code.google.com/p/hfr4droid/issues/detail?id=#25)
  * Modification du User-Agent pour régler le problème de connexion chez les usagers SFR
  * Correction de bugs divers

---

## 0.5.0 ##
### Ajouts ###
  * Ajout de la possibilité de changer de thème (pour l'instant thème de base, thème sombre et thème HFR 2011 gris) [issue #2](https://code.google.com/p/hfr4droid/issues/detail?id=#2)
  * Ajout du choix du type de notification pour le service ponctuel des mps (barre de statut ou toast) [issue #7](https://code.google.com/p/hfr4droid/issues/detail?id=#7)
  * Ajout de la possibilité d'afficher les avatars, images et smileys selon le type de connexion [issue #8](https://code.google.com/p/hfr4droid/issues/detail?id=#8)
  * Amélioration de l'interface de postage [issue #9](https://code.google.com/p/hfr4droid/issues/detail?id=#9)
  * Ajout d'un menu contextuel sur les images pour les enregistrer, partager ou visualiser [issue #13](https://code.google.com/p/hfr4droid/issues/detail?id=#13)
  * Ajout de la possibilité de retirer un drapeau d'un topic [issue #15](https://code.google.com/p/hfr4droid/issues/detail?id=#15)
  * Ajout de la navigation dans les sous-catégories via la page des catégories [issue #19](https://code.google.com/p/hfr4droid/issues/detail?id=#19)

### Bugs fixés ###
  * Correction du bug qui empêchait d'éditer le premier post [issue #11](https://code.google.com/p/hfr4droid/issues/detail?id=#11)
  * Correction de bugs divers [issue #12](https://code.google.com/p/hfr4droid/issues/detail?id=#12) [issue #22](https://code.google.com/p/hfr4droid/issues/detail?id=#22)

---

## 0.4.1 ##
### Bugs fixés ###
  * Correction du problème de rafraichissement de la liste des topics [issue #1](https://code.google.com/p/hfr4droid/issues/detail?id=#1)
  * Correction du bug dans la consultation du wiki smilies (lors de l'annulation pendant le chargement) [issue #6](https://code.google.com/p/hfr4droid/issues/detail?id=#6)

---

## 0.4.0 ##
### Ajouts ###
  * Possibilité de créer des mps, de partager des données par mp via d'autres applications
  * Possibilité de poster via la page des topics (via le menu contextuel d'un topic)
  * Support du "non lu" des mps et possibilité de mettre en "non lu" (via le menu contextuel d'un mp)
  * Ajout de l'indication "Nombre de pages en retard" pour chaque topic
  * Support des sous catégories
  * Implémentation d'un cache pour les catégories et sous-catégories
  * Nouvelle notification des mps : transparente, présente à tous les niveaux, peu ou pas consommatrice de bande passante. Consultez la documentation pour plus d'information

### Bugs fixés ###
  * Refactoring de la couche exception/message
  * Corrections de bugs mineurs divers

---

## 0.3.3 ##
### Bugs fixés ###
  * Adaptation du code suite à une modification du forum qui empêchait la lecture des topics et posts

---

## 0.3.2 ##
### Bugs fixés ###
  * Correction du bug lié aux pseudos contenant des espaces et autres caractères spéciaux (impossibilité de poster après avoir quitté l'application)

---

## 0.3.1 ##
### Bugs fixés ###
  * Correction du bug lié au plantage aléatoire de l'application quand on navigue dans un topic via le touchscreen

---

## 0.3.0 ##
### Ajouts ###
  * Possibilité de mettre un favori sur un post
  * Intégration de l'édition du wiki smilies (un clic sur le smiley permet d'éditer ses mots clés)
  * Intégration de hfr-rehost, feature par freds45
  * L'icône des posts générés depuis HFR4droid est désormais personnalisée
  * Absence de rechargement de la page si on post depuis une autre page que la dernière page

### Bugs fixés ###
  * Support du mot de passe incorrect (si on change son mot de passe dans HFR)
  * Bug dans le parsing des urls des notifications par mail des mps
  * Bug sur le clic long sur un nom de catégorie dans la page "Toutes les catégories"
  * Support des posts de la modération

---

## 0.2.1 ##
### Ajouts ###
  * Possibilité de désactiver le refresh sur double tap
  * Support de la maintenance du forum
### Bugs fixés ###
  * Stockage des cookies hors de la carte SD
  * Bug d'affichage dans la liste des topics (absence des "...")
  * Bug dans certaines urls du forum (contenant un numreponse différent de 0)
  * Bug quand dans le parsing des urls foireuses (écran noir)

---

## 0.2.0 ##
### Ajouts ###
  * Préchargement de la page suivante / précédente (activable ou non dans les paramètres)
  * Nouveau header plus compact, plus beau, avec défilement du titre possible
  * Mapping des urls du forum, en interne (quote), et en externe (lien http://forum.hardware.fr/*)
  * 4 nouveaux paramètres : activation ou non sa signature, mode fullscreen, choix de la taille des polices (3 tailles), choix de la sensibilité du swipe (3 niveaux)
  * Ajout des options "Copier le lien de ce post" et "Partager le lien de ce post" dans la barre d'outil de chaque post.
  * Intégration d'un envoi automatique de rapports d'erreurs (désactivé par défaut)
  * Ajout du refresh de la page sur double tap
  * Support de toutes les résolutions (visibilité market, fullscreen dans les tablettes)
  * Ajout d'un indice visuel de fin de topic
### Bugs fixés ###
  * Support de l'activation du clavier physique (plus de rechargement de page)
  * Le bouton physique back ne fermait pas la navigation sur la listes des topics
  * Le bandeau de titre de la page des mps ne changeait pas quand on venait de lire un nouveau mp
  * Mise à jour du code suite à la modification du code HTML du forum

---

## 0.1.0 ##
### Ajouts ###
  * Parcours du forum (drapeaux, favoris, messages privés)
  * Possibilité de poster, éditer, quoter, supprimer des posts
  * Support du wiki smilies
  * Service de notification des nouveaux mps
### Bugs fixés ###
  * Aucun :D