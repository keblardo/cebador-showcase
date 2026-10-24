# © 2026 Cristian Luis Celie. All rights reserved. Portfolio excerpt from Cebador: shared for review only, not licensed for use, copying or redistribution.
import os
import sys
import traceback

LOG_DIR = os.path.dirname(os.path.abspath(sys.executable if getattr(sys, 'frozen', False) else __file__))
LOG_PATH = os.path.join(LOG_DIR, 'app.log')

def log(msg):
    try:
        with open(LOG_PATH, 'a', encoding='utf-8') as f:
            f.write(str(msg) + '\n')
    except Exception:
        pass

try:
    log('starting')
    import webview
    log('webview imported')

    if getattr(sys, 'frozen', False):
        BASE_DIR = sys._MEIPASS
    else:
        BASE_DIR = os.path.dirname(os.path.abspath(__file__))

    HTML_PATH = os.path.join(BASE_DIR, 'cebador.html')
    DIARIO_PATH = os.path.join(BASE_DIR, 'diario.html')
    log('html path: ' + HTML_PATH + ' exists=' + str(os.path.exists(HTML_PATH)))
    log('diario path: ' + DIARIO_PATH + ' exists=' + str(os.path.exists(DIARIO_PATH)))

    diario_window = {'ref': None}

    class Api:
        def open_diario(self):
            log('open_diario called')
            w = diario_window['ref']
            if w is not None:
                try:
                    w.restore()
                    return True
                except Exception:
                    pass
            diario_window['ref'] = webview.create_window(
                'Diario',
                DIARIO_PATH,
                width=980,
                height=680,
                x=280,
                y=50,
                resizable=True,
            )
            return True

    window = webview.create_window(
        'Cebador',
        HTML_PATH,
        width=480,
        height=290,
        x=1040,
        y=16,
        resizable=True,
        on_top=True,
        js_api=Api(),
    )
    log('window created, starting loop')
    webview.start(debug=False)
    log('webview.start returned normally (window closed)')
except Exception:
    log('EXCEPTION:\n' + traceback.format_exc())
