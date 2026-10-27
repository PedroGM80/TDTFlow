#!/usr/bin/env python3
"""Genera la ficha de Google Play de TDTFlow (formato fastlane) y valida longitudes."""
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'metadata', 'android')

ES_FULL = """Ve la televisión TDT española y escucha la radio en directo, gratis y sin registrarte. Abre la app, elige un canal y listo: en el móvil, en la tablet, en tu Android TV o Google TV, o en la tele grande con Chromecast.

Sin cuentas, sin suscripciones y sin publicidad.

MÁS DE 100 CANALES DE TDT
Canales nacionales, autonómicos y temáticos de libre difusión, ordenados por categorías: General, Noticias, Deportes, Infantil, Entretenimiento, Regional, Música y más. Encuentra cualquiera al instante con el buscador.

MÁS DE 50 EMISORAS DE RADIO
Escucha la radio en directo con la pantalla apagada: reproducción en segundo plano con controles en la notificación.

EN LA TELE GRANDE
• Chromecast y Google Cast: manda cualquier canal a tu televisor con un toque.
• App para Android TV y Google TV pensada para el mando a distancia.
• Picture-in-Picture: sigue viendo un canal en una ventana flotante mientras usas otras apps.

A TU MEDIDA
• Favoritos para tener tus canales a mano, con copia de seguridad para importarlos y exportarlos.
• Búfer configurable (rápido, equilibrado o estable) según tu conexión.
• Detección de canales caídos, con reintento y lista de respaldo.
• Tema claro, oscuro o automático.
• App en español, catalán e inglés.

SOBRE LOS CANALES
TDTFlow solo reproduce emisiones oficiales de libre difusión. Algunos grupos audiovisuales, como Atresmedia y Mediaset, no permiten usar sus emisiones en aplicaciones de terceros, por eso no aparecen. La disponibilidad de algunos canales puede depender de tu ubicación.

TDTFlow está hecha por un desarrollador independiente. Si te gusta, una valoración en Google Play ayuda muchísimo."""

EN_FULL = """Watch Spanish free-to-air TV (TDT) and listen to Spanish radio live, for free and without signing up. Open the app, pick a channel and you're watching: on your phone, tablet, Android TV or Google TV, or on the big screen with Chromecast.

No accounts, no subscriptions and no ads.

100+ SPANISH TV CHANNELS
National, regional and thematic free-to-air channels from Spain, sorted into categories: General, News, Sports, Kids, Entertainment, Regional, Music and more. Find any channel instantly with search.

50+ RADIO STATIONS
Listen to Spanish radio live with the screen off: background playback with notification controls.

ON THE BIG SCREEN
• Chromecast and Google Cast: send any channel to your TV in one tap.
• Android TV and Google TV app designed for the remote control.
• Picture-in-Picture: keep watching in a floating window while you use other apps.

MADE FOR YOU
• Favourites to keep your channels at hand, with backup import and export.
• Configurable buffer (fast, balanced or stable) for your connection.
• Detects channels that are down, with retry and a backup list.
• Light, dark or automatic theme.
• App in English, Spanish and Catalan.

ABOUT THE CHANNELS
TDTFlow only plays official free-to-air streams. Some broadcasters, such as Atresmedia and Mediaset, do not allow their streams in third-party apps, so they are not included. Some channels may only be available in Spain.

TDTFlow is made by an independent developer. If you like it, a rating on Google Play helps a lot."""

EN_US_FULL = EN_FULL.replace('Favourites', 'Favorites')

CA_FULL = """Mira la televisió TDT espanyola i escolta la ràdio en directe, gratis i sense registrar-te. Obre l'app, tria un canal i ja està: al mòbil, a la tauleta, a l'Android TV o Google TV, o a la tele gran amb Chromecast.

Sense comptes, sense subscripcions i sense publicitat.

MÉS DE 100 CANALS DE TDT
Canals estatals, autonòmics i temàtics de lliure difusió, inclosos TV3 i els canals de 3Cat, ordenats per categories: General, Notícies, Esports, Infantil, Entreteniment, Regional, Música i més. Troba'n qualsevol a l'instant amb el cercador.

MÉS DE 50 EMISSORES DE RÀDIO
Escolta la ràdio en directe amb la pantalla apagada: reproducció en segon pla amb controls a la notificació.

A LA TELE GRAN
• Chromecast i Google Cast: envia qualsevol canal al televisor amb un toc.
• App per a Android TV i Google TV pensada per al comandament a distància.
• Picture-in-Picture: continua mirant un canal en una finestra flotant mentre fas servir altres apps.

AL TEU GUST
• Preferits per tenir els teus canals a mà, amb còpia de seguretat per importar-los i exportar-los.
• Memòria intermèdia configurable (ràpida, equilibrada o estable) segons la teva connexió.
• Detecció de canals caiguts, amb reintent i llista de reserva.
• Tema clar, fosc o automàtic.
• App en català, castellà i anglès.

SOBRE ELS CANALS
TDTFlow només reprodueix emissions oficials de lliure difusió. Alguns grups audiovisuals, com Atresmedia i Mediaset, no permeten fer servir les seves emissions en aplicacions de tercers, per això no hi surten. La disponibilitat d'alguns canals pot dependre de la teva ubicació.

TDTFlow l'ha fet un desenvolupador independent. Si t'agrada, una valoració a Google Play ajuda moltíssim."""

GL_FULL = """Mira a televisión TDT española e escoita a radio en directo, de balde e sen rexistrarte. Abre a app, escolle unha canle e listo: no móbil, na tableta, na túa Android TV ou Google TV, ou na tele grande con Chromecast.

Sen contas, sen subscricións e sen publicidade.

MÁIS DE 100 CANLES DE TDT
Canles estatais, autonómicas e temáticas de libre difusión, ordenadas por categorías: Xeral, Noticias, Deportes, Infantil, Entretemento, Rexional, Música e máis. Atopa calquera ao instante co buscador.

MÁIS DE 50 EMISORAS DE RADIO
Escoita a radio en directo coa pantalla apagada: reprodución en segundo plano con controis na notificación.

NA TELE GRANDE
• Chromecast e Google Cast: envía calquera canle á túa televisión cun toque.
• App para Android TV e Google TV pensada para o mando a distancia.
• Picture-in-Picture: segue vendo unha canle nunha xanela flotante mentres usas outras apps.

Á TÚA MEDIDA
• Favoritos para ter as túas canles a man, con copia de seguranza para importalos e exportalos.
• Búfer configurable (rápido, equilibrado ou estable) segundo a túa conexión.
• Detección de canles caídas, con reintento e lista de reserva.
• Tema claro, escuro ou automático.
• App en castelán, catalán e inglés.

SOBRE AS CANLES
TDTFlow só reproduce emisións oficiais de libre difusión. Algúns grupos audiovisuais, como Atresmedia e Mediaset, non permiten usar as súas emisións en aplicacións de terceiros, por iso non aparecen. A dispoñibilidade dalgunhas canles pode depender da túa localización.

TDTFlow está feita por un desenvolvedor independente. Se che gusta, unha valoración en Google Play axuda moitísimo."""

EU_FULL = """Ikusi Espainiako TDT telebista eta entzun irratia zuzenean, doan eta erregistratu gabe. Ireki aplikazioa, aukeratu kanal bat eta kitto: mugikorrean, tabletan, Android TV edo Google TV-n, edo telebista handian Chromecast-ekin.

Konturik gabe, harpidetzarik gabe eta publizitaterik gabe.

100 TDT KANAL BAINO GEHIAGO
Doako kanal estatalak, autonomikoak eta tematikoak, ETB barne, kategoriatan antolatuta: Orokorra, Albisteak, Kirolak, Haurrak, Entretenimendua, Eskualdekoa, Musika eta gehiago. Bilatzailearekin edozein kanal berehala aurkituko duzu.

50 IRRATI-KATE BAINO GEHIAGO
Entzun irratia zuzenean pantaila itzalita: atzeko planoko erreprodukzioa, jakinarazpeneko kontrolekin.

TELEBISTA HANDIAN
• Chromecast eta Google Cast: bidali edozein kanal zure telebistara ukitu batekin.
• Android TV eta Google TV-rako aplikazioa, urrutiko agintearekin erabiltzeko pentsatua.
• Picture-in-Picture: jarraitu kanal bat ikusten leiho flotagarri batean beste aplikazio batzuk erabiltzen dituzun bitartean.

ZURE NEURRIRA
• Gogokoak, zure kanalak eskura izateko, inportatu eta esportatzeko babeskopiarekin.
• Buffer konfiguragarria (azkarra, orekatua edo egonkorra) zure konexioaren arabera.
• Erorita dauden kanalak detektatzen ditu, berriro saiatzeko aukerarekin eta ordezko zerrendarekin.
• Gai argia, iluna edo automatikoa.
• Aplikazioa gaztelaniaz, katalanez eta ingelesez.

KANALEI BURUZ
TDTFlow-k doako emisio ofizialak bakarrik erreproduzitzen ditu. Ikus-entzunezko talde batzuek, Atresmedia eta Mediasetek adibidez, ez dute uzten haien emisioak hirugarrenen aplikazioetan erabiltzen, eta horregatik ez daude. Kanal batzuen erabilgarritasuna zure kokapenaren araberakoa izan daiteke.

TDTFlow garatzaile independente batek egina da. Gustatzen bazaizu, Google Play-ko balorazio batek asko laguntzen du."""

LISTINGS = {
    'es-ES': ('TDTFlow: TV y Radio en Directo',
              'Ver la TDT y la radio en directo gratis, con Chromecast y Android TV.',
              ES_FULL),
    'en-US': ('TDTFlow: Spanish TV & Radio',
              'Watch Spanish TV and radio live for free, with Chromecast and Android TV.',
              EN_US_FULL),
    'en-GB': ('TDTFlow: Spanish TV & Radio',
              'Watch Spanish TV and radio live for free, with Chromecast and Android TV.',
              EN_FULL),
    'ca': ('TDTFlow: TV i Ràdio en Directe',
           'Mira la TDT i escolta la ràdio en directe gratis, amb Chromecast i Android TV.',
           CA_FULL),
    'gl-ES': ('TDTFlow: TV e Radio en Directo',
              'Mira a TDT e escoita a radio en directo de balde, con Chromecast e Android TV.',
              GL_FULL),
    'eu-ES': ('TDTFlow: Telebista eta Irratia',
              'Ikusi TDT eta entzun irratia zuzenean doan, Chromecast eta Android TV-rekin.',
              EU_FULL),
}

LIMITS = (30, 80, 4000)

for lang, texts in LISTINGS.items():
    d = os.path.join(ROOT, lang)
    os.makedirs(d, exist_ok=True)
    for name, text, limit in zip(('title', 'short_description', 'full_description'), texts, LIMITS):
        assert len(text) <= limit, f'{lang} {name}: {len(text)} > {limit}'
        with open(os.path.join(d, f'{name}.txt'), 'w', encoding='utf-8') as f:
            f.write(text + '\n')
    print(lang, *(len(t) for t in texts))
