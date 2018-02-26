#!/usr/bin/env python

"""
    Software Laboratory 5
    SOA service demo

    This sample service was created for the purposes of demonstrating some
    of the functionality you can achieve by combining the power of three
    Python libraries: cx_Oracle, Flask, and Requests.

    It does not intend to be perfect Python code -- in some places, perfection
    was traded for simplicity, some of these are marked in comments.

    This comment is a so-called docstring, all Python modules and
    functions/methods should have one. Three " or ' characters make it
    possible for multiline strings, and interactive Python environments
    display these "docstrings" (basically header comments) for users of
    your code. Further info: http://www.python.org/dev/peps/pep-0257/
"""

from flask import Flask, jsonify, abort, request
from datetime import datetime
import json
import cx_Oracle
import requests

app = Flask(__name__)

@app.route('/szemelyek.json')
def list_people():
    """Lists the first 50 persons in the database"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Note: don't use prefixes like "oktatas." above for tables
            # within your own schema, as it ruins portability.
            # This table has 10k rows, so we intentionally limit the result set to 50
            # (Oracle note: not the first 50 rows by name, but rather
            # the first 50 rows of the table, which are then ordered by name).
            # Also, long queries can be broken into two shorter lines like this
            cur.execute('''SELECT szemelyi_szam, nev FROM oktatas.szemelyek
                WHERE ROWNUM < 50 ORDER BY nev ASC''')
            # there's a better way, but outside the scope of this lab:
            # http://docs.python.org/2/tutorial/datastructures.html#list-comprehensions
            results = []
            # we make use of the fact that
            #  - cursors are iterable and
            #  - `for` can unpack objects returned by each iteration
            for szemelyi_szam, nev in cur:
                results.append({'szemelyi_szam': szemelyi_szam, 'nev': nev})
            return jsonify(szemelyek=results)
        finally:
            cur.close()
    finally:
        # this is also a naive implementation, a more Pythonic solution:
        # http://docs.python.org/2/library/contextlib.html#contextlib.closing
        conn.close()

@app.route('/szemely/<szemelyi_szam>.json')
def show_person(szemelyi_szam):
    """Shows the details of a single person by szemelyi_szam"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Note: don't use prefixes like "oktatas." above for tables
            # within your own schema, as it ruins portability
            cur.execute('SELECT nev FROM oktatas.szemelyek WHERE szemelyi_szam = :sz',
                        sz=szemelyi_szam)
            # fetchone() returns a single row if there's one, otherwise None
            result = cur.fetchone()
            # in Python '==' compares by value, 'is' compares by reference
            # (of course, former would work too, but it's slower and unnecessary)
            # 'None' is the Python version of null, it's a singleton object, so
            # we can safely compare to it using 'is' (Java/C#: result == null)
            if result is None:
                # no rows -> 404 Not Found (no need to return manually)
                abort(404)
            else:
                links = []
                try:
                    # we query the Wikipedia API to see what happened the day
                    # the person was born based on szemelyi_szam
                    born = datetime.strptime(szemelyi_szam[1:7], '%y%m%d')
                    params = {'action': 'query',
                              'titles': born.strftime('%B %d').replace('0', ''),
                              'prop': 'extlinks',
                              'format': 'json',}
                    # API docs: http://www.mediawiki.org/wiki/API:Tutorial
                    res = requests.get('https://en.wikipedia.org/w/api.php', params=params)
                    for page in res.json()['query']['pages'].itervalues():
                        for link in page['extlinks']:
                            for href in link.itervalues():
                                links.append(href)
                except:
                    pass # necessary if a clause would be empty in Python

                # result set rows can be indexed too
                return jsonify(nev=result[0], links=links)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/datetest.json')
def date_test():
    """Datumtest"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Note: don't use prefixes like "oktatas." above for tables
            # within your own schema, as it ruins portability
            # http://www.oracle.com/technetwork/articles/dsl/prez-python-timesanddates-093014.html
            # https://docs.python.org/2/library/datetime.html
            # it's casted automatically to datetime
            cur.execute('SELECT datum, usd FROM oktatas.mnb_deviza where id < 10')
            results = []
            for datum, usd in cur:
                results.append({'datum': datum, 'datum_iso' : datum.isoformat(), 'usd': usd})
            return jsonify(arfolyamok=results)
        finally:
            cur.close()
    finally:
        conn.close()


@app.route('/verbtest.json', methods=['PUT', 'POST'])
def verb_test():
    """Lets you test HTTP verbs different from GET, expects and returns data in JSON format"""
    # it also shows you how to access the method used and the decoded JSON data
    return jsonify(method=request.method, data=request.json, url=request.url)

def get_db():
    """Connects to the RDBMS and returns a connection object"""
    # when used with a `file` object, `with` ensures it gets closed
    with file('config.json') as config_file:
        config = json.load(config_file)
    return cx_Oracle.connect(config['user'], config['pass'], config['host'])

@app.route("/eszkozok/<eszk_azon>", methods=['DELETE', 'PUT'])
def myverb1(eszk_azon):
    """5. Feladat - HTTP verbek : Eloszor DELETE es PUT utasitasok /eszkozok/<eszk_azon> cimrol"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Ezzel az SQL lekerdezessel ellenorizhetjuk,
            # hogy valid-e a megadott azonosito
            cur.execute("SELECT eszk_azon FROM eszkozok WHERE eszk_azon" +
                        "= :eszk_azon", eszk_azon=eszk_azon)
            result = cur.fetchone()
            if result is None:
                # Ha nincs egyezes, jelezzuk a felhasznalonak
                abort(404)
            else:
                if request.method == 'DELETE':
                    # DELETE verb implementalasa
                    try:
                        # Mivel a valtoztatast el is akarjuk menteni, itt megnyitjuk a kapcsolatot
                        conn.begin()
                        # Itt a parameteres SQL utasitas, amely kitorli a
                        # megfelelo azonositoju eszkozt
                        cur.execute("DELETE FROM eszkozok WHERE " +
                                    "eszk_azon = :eszk_azon", eszk_azon=eszk_azon)
                        # A valtoztatast commitoljuk
                        conn.commit()
                    except:
                        # Ha hiba tortent a vegrehajtas soran, rollbackeljen
                        conn.rollback()
                        # Es hibat is dobunk a felhasznalonak
                        abort(500)
                elif request.method == 'PUT':
                    # PUT verb implementalasa
                    # Eloszor lementjuk az atadott adatokat
                    data = request.json
                    try:
                        conn.begin()
                        # Ezzel az SQL utasitassal tudjuk frissiteni
                        # az adatbazist, a megfelelo helyen
                        cur.execute("UPDATE eszkozok SET nev = :nev, marka = :marka" +
                                    ", tipus = :tipus, napi_ksg = :napi_ksg, vasarlas =" +
                                    " TO_DATE(:vasarlas, 'yyyy-MM-dd') WHERE eszk_azon" +
                                    " = :eszk_azon", nev=data['nev'],
                                    marka=data['marka'], tipus=data['tipus'],
                                    napi_ksg=data['napi_ksg'], vasarlas=data['vasarlas'],
                                    eszk_azon=eszk_azon)
                        conn.commit()
                        # A feladat szerint visszaadjuk a modositott eszkoz azonositojat
                        return jsonify({"eszk_azon": eszk_azon})
                    except:
                        # Hiba eseten rollback es uzenunk a felhasznalonak
                        conn.rollback()
                        abort(500)
        finally:
            # Ha befejezodott a futasa a fuggveny tobbi reszenek, bezarjuk a cursor-t
            cur.close()
    finally:
        # Es a connection-t is
        conn.close()

@app.route("/eszkozok.json", methods=['POST'])
def myverb2():
    """POST verb implementalasa a /eszkozok.json cimen"""
    # Leellenorizzuk az utasitast
    if request.method == 'POST':
    # Felvesszuk a kapcsolatot es lekerjuk az adatokat is
        conn = get_db()
        data = request.json
        try:
            cur = conn.cursor()
            try:
                # Generaljuk az azonositot a segedfuggveny segitsegevel
                eszk_azon = gen_azon()
                # Modositani akarjuk a tablat innentol:
                conn.begin()
                # Az alabbi SQL utasitassal veszi fel az uj eszkozt
                cur.execute("INSERT INTO eszkozok VALUES (:eszk_azon, :nev, :marka," +
                            ":tipus,  :napi_ksg, TO_DATE(:vasarlas," +
                            " 'yyyy-MM-dd'))", eszk_azon=eszk_azon, nev=data['nev'],
                            tipus=data['tipus'], marka=data['marka'], napi_ksg=data['napi_ksg'],
                            vasarlas=data['vasarlas'])
                conn.commit()
                # A feladat szovege szerint visszaadjuk a felvett eszkoz azonositojat
                return jsonify({"eszk_azon": eszk_azon})
            except:
                # Hiba eseten rollback es a felhasznalo ertesitese
                conn.rollback()
                abort(500)
            finally:
                cur.close()
        finally:
            conn.close()

def gen_azon():
    """Segedfuggveny, mellyel azonositot tud generalni a POST verb"""
    # Ehhez eloszor le ker kerdeznunk, hogy mar
    # milyen azonositok vannak hasznalatban
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Ezzel az SQL lekerdezessel meg is van errol egy lista
            cur.execute("SELECT eszk_azon FROM eszkozok")
            # Ebben a valtozoban taroljuk a jelenlegi
            # legnagyobb eszk_azon erteket, a szokezdo "E" nelkul
            biggestvalue = 0
            # Vegigiteralunk az eredmenytablan, es minden eszk_azon-ra
            # megnezzuk, hogy nagyobb-e, mint a biggestValue
            # es ha nagyobb -> atirjuk az erteket
            for eszk_azon in cur:
                actualvalue = int((''.join(eszk_azon)).replace("E", ""))
                if actualvalue > biggestvalue:
                    biggestvalue = actualvalue
            # A ciklus vegere a legnagyobb eszk_azon-nal lesz egyerteku a
            # biggestValue, igy ezt novelve mar biztos hogy egyedi lesz
            biggestvalue = biggestvalue + 1
            # Aszerint, hogy hany 0-t kell a szam ele irni
            # kiegeszitjuk, es string-ge alakitjuk
            if biggestvalue > 99:
                eszk_azon = "E" + str(biggestvalue)
            elif biggestvalue > 9:
                eszk_azon = "E0" + str(biggestvalue)
            else:
                eszk_azon = "E00" + str(biggestvalue)
            # A visszaadott mar egy egyedi string
            return eszk_azon
        finally:
            cur.close()
    finally:
        conn.close()

@app.route("/eszkozok.json")
def eszkozok():
    """1. feladat 1. resz"""
    # Megnyitjuk az adatbazis kapcsolatot
    conn = get_db()
    try:
        # Letrehozunk egy cursort az adatbazisban a navigalashoz
        cur = conn.cursor()
        try:
            # Ezzel az SQL lekerdezessel kapjuk meg az eredmenytabla megfelelo adatait
            cur.execute('SELECT eszk_azon, nev, vasarlas FROM eszkozok')
            # Ezt a tombot fogjuk visszaadni
            results = []
            # Vegigiteralunk az eredmenytablan, es minden sort felveszunk
            # az eredmenytombbe, a megfelelo formatumban
            # A vasarlas attributuma az eredmenytablanak alapvetoen SQL.Date
            # tipusu, ezt ugy regisztraljuk mint amin nincs idopont
            # a date() metodussal, majd az igy kapott formatumot mar a
            # JSON-nal kompatibilisre tudjuk hozni az isoformat()-tal
            for eszk_azon, nev, vasarlas in cur:
                results.append({"eszk_azon": eszk_azon, "nev": nev,
                                "vasarlas": vasarlas.date().isoformat()})
            # Visszaadjuk az eredmenytombot
            return jsonify(eszkozok=results)
        finally:
            # Bezarjuk a cursor-t
            cur.close()
    finally:
        # Es a kapcsolatot is
        conn.close()

@app.route("/eszkozok/<eszk_azon>.json")
def eszkozok_w_azon(eszk_azon):
    """1. feladat 2. resz"""
    # Megnyutjuk a kapcsolatot
    conn = get_db()
    try:
        # Keszitunk egy cursort
        cur = conn.cursor()
        try:
            # Ezzel a parameteres SQL lekerdezessel lehet
            # lekerdezni a megfelelo azonositoju eszkozoket
            cur.execute('SELECT nev, marka, tipus, napi_ksg, vasarlas FROM eszkozok WHERE' +
                        ' eszk_azon = :eszk_azon', eszk_azon=eszk_azon)
            # Ebben a valtozoban lesz az eredmenytabla egyetlen
            # sora (biztos hogy 1, hiszen az eszk_azon kulcs az eszkozokben)
            result = cur.fetchone()
            # Ha nem talaltunk ilyen azonositoju eszkozt, szolunk a felhasznalonak
            if result is None:
                abort(404)
            else:
                # Ha van ilyen eszkoz, akkor kiszamitjuk a napi koltseget euroban is
                # Ehhez az alabbi tavoli szolgaltatassal lekerdezzuk, hogy 1 forint hany euro
                response = requests.get("http://currencies.apps.grandtrunk.net/getlatest/huf/eur")
                # A mezo formatuma string lesz, es ugy kaphatjuk
                # meg, hogyha osszeszorozzuk a napi_ksg mezovel, ami forintban tarolja
                napi_ksg_eur = str(float(response.text)*int(result[3]))
                # Visszaterunk a JSON formatumu dictionary-vel,
                # ami mindent a megfelelo formatumban tarol
                return jsonify({"nev": result[0], "marka": result[1], "tipus": result[2],
                                "napi_ksg": result[3], "vasarlas": result[4].date().isoformat(),
                                "napi_ksg_eur": napi_ksg_eur})
        finally:
            cur.close()
    finally:
        conn.close()

@app.route("/eszkozok/tipus-szerint/<tipus>.json")
def kereses_tipus_szerint(tipus):
    """4. feladat kereses tipus szerint"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # A keresesi feltetel kiegeszitese nehany olyan dologgal, ami fontos
            # Itt teszem lehetove, hogy a specialis karakterekre lehessen keresni
            # Ezt ugy teszem lehetove, hogy az SQL lekerdezesben az @ karaktert ESCPAE-elem,
            # vagyis az utana kovetkezo karakter egyszeru
            # karakterkent lesz kiertekelve, nem pedig specialiskent
            # Eloszor a mar meglevo @ karaktereket duplazom meg
            tipus = tipus.replace("@", "@@")
            # Majd johet a ket specialis karakter, a % es az _
            tipus = tipus.replace("%", "@%")
            tipus = tipus.replace("_", "@_")
            # Ezek utan, hogy lehessen keresni csonka szavakra is, kiegeszitem a keresesi feltetel
            # mindket oldalat egy-egy %-el,
            # ami itt mar specialis karakterkent fog szerepelni
            tipus = "%" + tipus + "%"
            # Ezzel az SQL lekerdezessel kapjuk meg a
            # keresesi feltetelnek megfeleloen az eredmenytablat
            cur.execute("SELECT eszk_azon, nev, vasarlas FROM eszkozok WHERE tipus" +
                        " LIKE :tipus ESCAPE '@'", tipus=tipus)
            # Ebben a tombben tarolja majd az eredmenytabla sorait, mar a megfelelo formatumban
            results = []
            for eszk_azon, nev, vasarlas in cur:
                results.append({"eszk_azon": eszk_azon, "nev": nev,
                                "vasarlas": vasarlas.date().isoformat()})
            # Ha nincs eredmenye a lekerdezesnek, akkor azt jelezzuk a felhasznalonak
            if results is None:
                abort(404)
            else:
                return jsonify(eszkozok=results)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route("/eszkozok/nev-szerint/<nev>.json")
def kereses_nev_szerint(nev):
    """4. feladat kereses nev szerint"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            nev = nev.replace("@", "@@")
            nev = nev.replace("%", "@%")
            nev = nev.replace("_", "@_")
            nev = "%" + nev + "%"
            cur.execute("SELECT eszk_azon, nev, vasarlas FROM eszkozok WHERE nev " +
                        "LIKE :nev ESCAPE '@'", nev=nev)
            results = []
            for eszk_azon, nev, vasarlas in cur:
                results.append({"eszk_azon": eszk_azon, "nev": nev,
                                "vasarlas": vasarlas.date().isoformat()})
            if results is None:
                abort(404)
            else:
                return jsonify(eszkozok=results)
        finally:
            cur.close()
    finally:
        conn.close()

if __name__ == "__main__":
    import os
    os.environ['NLS_LANG'] = '.UTF8'
    app.run(debug=True, port=os.getuid() + 10000)
