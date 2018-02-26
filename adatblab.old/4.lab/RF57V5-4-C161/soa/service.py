#!/usr/bin/env python
# pylint: disable=invalid-name

"""
    Databases Laboratory 5
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

from datetime import datetime
import json

from flask import Flask, jsonify, abort, request
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
            links = []
            try:
                # we query the Wikipedia API to see what happened the day
                # the person was born based on szemelyi_szam
                born = datetime.strptime(szemelyi_szam[1:7], '%y%m%d')
                params = {
                    'action': 'query',
                    # 2012-04-01 -> "April 01" -> "April 1"
                    'titles': born.strftime('%B %d').replace('0', ''),
                    'prop': 'extlinks',
                    'format': 'json',
                    }
                # API docs: http://www.mediawiki.org/wiki/API:Tutorial
                # Example for 1st April:
                # https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extlinks&titles=April%201
                res = requests.get('https://en.wikipedia.org/w/api.php', params=params)
                for page in res.json()['query']['pages'].itervalues():
                    for link in page['extlinks']:
                        for href in link.itervalues():
                            links.append(href)
            except IOError:
                pass # necessary if a clause would be empty in Python

            # result set rows can be indexed too
            return jsonify(nev=result[0], links=links)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/datetest.json')
def date_test():
    """Demonstrates handling dates from databases and formatting it according to ISO 8601"""
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
    return jsonify(method=request.method, data=request.get_json(), url=request.url)


def get_db():
    """Connects to the RDBMS and returns a connection object"""
    # when used with a `file` object, `with` ensures it gets closed
    # pylint: disable=no-member
    with file('config.json') as config_file:
        config = json.load(config_file)
    return cx_Oracle.connect(config['user'], config['pass'], config['host'])

# 1. feladat
@app.route('/orders.json')
def orders():
    """1. feladat - /orders.json implementalasa, mely kilistazza az osszes megrendelest"""
    # Megnyitjuk az adatbazis kapcsolatot
    conn = get_db()
    try:
        # Letrehozunk egy cursort az adatbazisban a navigalashoz
        cur = conn.cursor()
        try:
            # Ezzel az SQL lekerdezessel kapjuk meg az eredmenytabla megfelelo adatait
            cur.execute('SELECT order_id, description, quantity, deadline_date FROM orders')
            # Ezt a tombot fogjuk visszaadni
            results = []
            # Vegigiteralunk az eredmenytablan, es minden sort felveszunk
            # az eredmenytombbe, a megfelelo formatumban
            # A megrendeles lejarta az eredmenytablanak alapvetoen SQL.Date
            # tipusu, ezt ugy regisztraljuk mint amin nincs idopont
            # a date() metodussal, majd az igy kapott formatumot mar a
            # JSON-nal kompatibilisre tudjuk hozni az isoformat()-tal
            for order_id, description, quantity, deadline_date in cur:
                results.append({"order_id": order_id,
                                "description": description,
                                "quantity": quantity,
                                "deadline_date": deadline_date.date().isoformat()})
            # Visszaadjuk az eredmenytombot
            return jsonify(orders=results)
        finally:
            # Bezarjuk a cursor-t
            cur.close()
    finally:
        # Bezarjuk a kapcsolatot-t
        conn.close()

@app.route('/orders/<path:order_id>.json')
def order_w_order_id(order_id):
    """
    1. feladat - /orders/<order_id>.json implementalasa, 
    mely lekeri a parameterkent kapott order_id-ju megrendeles adatait
    """
    # Megnyutjuk a kapcsolatot
    conn = get_db()
    try:
        # Keszitunk egy cursort
        cur = conn.cursor()
        try:
            # Ezt a parameteres SQL lekerdezest hajtjuk vegre, mellyel megkapjuk az adott
            # order_id-ju megrendelest.
            cur.execute('SELECT description, vehicle_type, quantity, origin, destination,' +
                        ' order_date, deadline_date, comment_text FROM orders WHERE' +
                        ' order_id = :order_id', order_id=order_id)
            # Ebben a valtozoban lesz az eredmenytabla egyetlen
            # sora (Biztosan 1 lesz, mert az order_id egyedi)
            result = cur.fetchone()
            # Ha nem talaltunk ilyen megrendelest, szolunk a felhasznalonak
            if result is None:
                abort(404)
            else:
                # 2. feladat - lekerdezzuk az adott orszag valutajat
                #
                # Az origin illetve destination mezokben megkeressuk az orszag betujelet
                # Ez mindig a string vegen, ( es ) jelek kozott allo 2 betu.
                # Mivel ezek nagybetuvel irodtak at kell konvertalnunk kisbeture.
                # Ezek futtatjuk a kerest, majd a kapott eredmenyt JSON formatumra parsoljuk.
                # Ebbol kiolvassuk a valuta erteket, amit majd atadunk a kimeneti mezonknek.
                origin001 = result[3]
                origin_len = len(origin001)
                origin_tmp = origin001[origin_len-3:origin_len-1]
                origin_url = "http://rapid.eik.bme.hu:9080/currency_ws/currencies/" + origin_tmp.lower() + ".json"
                r1 = requests.get(origin_url)
                var1 = r1.json()
                origin_currency = var1['currency']
                
                destination001 = result[4]
                destination_len = len(destination001)
                destination_tmp = destination001[destination_len-3:destination_len-1]
                destination_url = "http://rapid.eik.bme.hu:9080/currency_ws/currencies/" + destination_tmp.lower() + ".json"
                r2 = requests.get(destination_url)
                var2 = r2.json()
                destination_currency = var2['currency']
                # Visszaterunk a JSON formatumu dictionary-vel,
                # ami mindent a megfelelo formatumban tarol
                return jsonify({"description": result[0],
                                "vehicle_type": result[1],
                                "quantity": result[2],
                                "origin": result[3],
                                "destination": result[4],
                                "order_date": result[5].date().isoformat(),
                                "deadline_date": result[6].date().isoformat(),
                                "comment_text": result[7],
                                "origin_currency": origin_currency,
"destination_currency": destination_currency})
        finally:
            cur.close()
    finally:
        conn.close()

# 4. feladat
@app.route('/orders/by_description/<desc_pfx>.json')
def search_by_description(desc_pfx):
    """
    4. feladat - kereses megnevezes szerint implementalasa,
    mely a parameterkent kapot kulcsra keres a megrendelesek
    description attributumaban
    """
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
            desc_pfx = desc_pfx.replace("@", "@@")
            # Majd johet a ket specialis karakter, a % es az _
            desc_pfx = desc_pfx.replace("_", "@_")
            desc_pfx = desc_pfx.replace("%", "@%")
            # Ezek utan, hogy lehessen keresni csonka szavakra is, kiegeszitem a keresesi feltetel
            # mindket oldalat egy-egy %-el.
            desc_pfx = "%" + desc_pfx + "%"
            # Az SQL lekerdezes, amivel megkapjuk a
            # keresesi feltetelnek megfelelo eredmenytablat
            cur.execute("SELECT order_id, description, quantity, deadline_date FROM orders" +
                        " WHERE description LIKE :description ESCAPE '@'", description=desc_pfx)
            # Eredmenytomb
            results = []
            for order_id, description, quantity, deadline_date in cur:
                results.append({"order_id": order_id,
                                "description": description,
                                "quantity": quantity,
                                "deadline_date": deadline_date.date().isoformat()})
            # Ha nincs eredmeny, szolunk a felhasznalonak.
            if results is None:
                abort(404)
            else:
                return jsonify(orders=results)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/orders/by_type/<type>.json')
def search_by_type(type):
    """
    4. feladat - kereses jarmu tipusa szerint,
    mely a parameterkent kapot kulcsra keres a megrendelesek
    vehicle_type attributumaban
    """
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            type = type.replace("@", "@@")
            type = type.replace("%", "@%")
            type = type.replace("_", "@_")
            type = "%" + type + "%"
            cur.execute("SELECT order_id, description, quantity, deadline_date FROM orders WHERE vehicle_type " +
                        "LIKE :vehicle_type ESCAPE '@'", vehicle_type=type)
            results = []
            for order_id, description, quantity, deadline_date in cur:
                results.append({"order_id": order_id,
                                "description": description,
                                "quantity": quantity,
                                "deadline_date": deadline_date.date().isoformat()})
            if results is None:
                abort(404)
            else:
                return jsonify(orders=results)
        finally:
            cur.close()
    finally:
        conn.close()

# 5. feladat
@app.route('/orders/<order_id>', methods =['DELETE', 'PUT'])
def my_verb_1(order_id):
    """5. feladat - DELETE es PUT utasitasok implementalasa /orders/<order_id> cimrol"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Ellenorizzuk, hogy letezik e az adott order_id-ju megrendeles
            cur.execute("SELECT order_id FROM orders WHERE order_id" +
                        "= :order_id", order_id=order_id)
            result = cur.fetchone()
            if result is None:
                # Ha nincs egyezes, jelezzuk a felhasznalonak
                abort(404)
            else:
                if request.method == 'DELETE':
                    # DELETE verb implementalasa
                    try:
                        # A valtoztatast el is akarjuk menteni, ezert itt megnyitjuk a kapcsolatot
                        conn.begin()
                        # Parameteres SQL utasitassal kitoroljuk az adott order_id-ju megrendelest
                        cur.execute("DELETE FROM orders WHERE " +
                                    "order_id = :order_id", order_id=order_id)
                        # A valtoztatast commitoljuk
                        conn.commit()
                    except:
                        # Ha hiba tortent a vegrehajtas soran, rollback-et hajtunk vegre
                        conn.rollback()
                        # Majd hibat dobunk a felhasznalonak
                        abort(500)
                elif request.method == 'PUT':
                    # PUT verb implementalasa
                    # Eloszor mentjuk a kapott adatokat
                    data = request.json
                    try:
                        conn.begin()
                        # Az alabbi SQL utasitassal frissitjuk
                        # az adatbazist
                        cur.execute("UPDATE orders SET description = :description, vehicle_type = :vehicle_type, " +
                                    "quantity = :quantity, origin = :origin, destination = :destination, " +
                                    "order_date = TO_DATE(:order_date, 'yyyy-MM-dd'), " +
                                    "deadline_date = TO_DATE(:deadline_date, 'yyyy-MM-dd'), " +
                                    "comment_text = :comment_text " +
                                    "WHERE order_id = :order_id",
                                    description=data['description'],
                                    vehicle_type=data['vehicle_type'],
                                    quantity=data['quantity'],
                                    origin=data['origin'],
                                    destination=data['destination'],
                                    order_date=data['order_date'],
                                    deadline_date=data['deadline_date'],
                                    comment_text=data['comment_text'],
                                    order_id=order_id)
                        conn.commit()
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

@app.route("/orders.json", methods=['POST'])
def my_verb_2():
    """5. feladat - POST verb implementalasa /orders.json cimen"""
    # Leellenorizzuk az utasitast
    if request.method == 'POST':
    # Felvesszuk a kapcsolatot es lekerjuk az adatokat
        conn = get_db()
        data = request.json
        try:
            cur = conn.cursor()
            try:
                # Generaljuk az azonositot a segedfuggveny segitsegevel
                order_id = generate_order_id()
                # Modositani akarjuk a tablat innentol:
                conn.begin()
                # Felvesszuk az uj megrendelest
                cur.execute("INSERT INTO orders VALUES (:order_id, :description, :vehicle_type," +
                            ":quantity,  :origin, :destination, TO_DATE(:order_date, 'yyyy-MM-dd')), "+
                            "TO_DATE(:deadline_date, 'yyyy-MM-dd')), :comment_text",
                            order_id=order_id,
                            description=data['description'],
                            vehicle_type=data['vehicle_type'],
                            quantity=data['quantity'],
                            origin=data['origin'],
                            destination=data['destination'],
                            order_date=data['order_date'],
                            deadline_date=data['deadline_date'],
                            comment_text=data['comment_text'])
                conn.commit()
                # Visszaadjuk a felvett megrendeles azonositojat
                return jsonify({"order_id": order_id})
            except:
                # Hiba eseten rollback es a felhasznalo ertesitese
                conn.rollback()
                abort(500)
            finally:
                cur.close()
        finally:
            conn.close()

def gen_azon():
    """5. feladat - Segedfuggveny, mely egyedi azonositot general a POST verb szamara"""
    # Le kell  kerdeznunk, hogy milyen ID-k vannak hasznalatban
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Lekerdezzuk az osszes order_id-t
            cur.execute("SELECT order_id FROM orders")
            # Itt taroljuk a jelenlegi legnagyobb order_id-t 
            # (annak csak az utolso 6 betujet fogjuk vizsgalni)
            biggestvalue = 0
            # Vegigiteralunk az eredmenytablan, es minden order_id-ra
            # megnezzuk hogy az utolso 6 szamjegye nagyobb e mint a biggestValue.
            # Ha az ID-k kozott lenne 2016/ kezdetu is, nem lesz gond, hiszen a 
            # vizsgalat soran nem teszunk kulonbseget a kulonbozo evek kozott, igy 
            # az osszes evhez kepest egyedi ID jon letre, melyet 2017-kent regisztralunk majd.
            for order_id in cur:
                actualvalue = int(order_id[-6:])
                if actualvalue > biggestvalue:
                    biggestvalue = actualvalue
            # A ciklus vegere a biggestValue a legnagyobb order_id ertekkel lesz egyenlo,
            # igy ezt novelve mar biztos hogy egyedi erteket kapunk
            biggestvalue = biggestvalue + 1
            # Az uj ID ele 2017/-es elotagot teszunk
            order_id = '2017/'+str(biggestValue)
            return jsonify({"order_id": order_id})
        finally:
            cur.close()
    finally:
        conn.close()

if __name__ == "__main__":
    # pylint: disable=bad-option-value,wrong-import-position,wrong-import-order
    import os
    os.environ['NLS_LANG'] = '.UTF8'
    app.run(debug=True, port=os.getuid() + 10000)
