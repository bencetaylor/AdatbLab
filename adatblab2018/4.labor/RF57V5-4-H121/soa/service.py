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

@app.route('/allomasok.json')
def list_allomasok():
    """Kilistazza az allomasokat az adatbazisbol"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            cur.execute('SELECT id, nev, varos FROM allomas')
            # there's a better way, but outside the scope of this lab:
            # http://docs.python.org/2/tutorial/datastructures.html#list-comprehensions
            results = []
            
            for id, nev, varos in cur:
                results.append({'id': id, 'nev': nev, 'varos': varos})
            return jsonify(allomasok=results)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/allomasok/<id>.json')
def show_allomas(id):
    """ Kiirja a parameterkent kapott id-ju allomas adatait"""
    # Allomas kiirasa id alapjan + bovitve 2. feladat (koordinatak kiiratasa)
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            cur.execute('SELECT * FROM allomas WHERE id = :allomas_id',allomas_id=id)
            result = cur.fetchone()
            if result is None:
                # no rows -> 404 Not Found (no need to return manually)
                abort(404)
            # 2. feladat megoldasa:
            try:
                params = {'format': 'json', 'city': result[2], 'country': 'Hungary'}
                res = requests.get('http://nominatim.openstreetmap.org/search', params=params)
                coordinates = res.json()
                for coordinate in coordinates:
                    lat = coordinate['lat']
                    lon = coordinate['lon']
            except IOError:
                pass # necessary if a clause would be empty in Python
            # result set rows can be indexed too
            return jsonify(nev=result[1], varos=result[2], atlagutas=result[3], sztrajkutas=result[4], szelesseg=lat, hosszusag=lon)
        finally:
            cur.close()
    finally:
        conn.close()

# 4. feladat megoldasai:
@app.route('/allomasok/varos-szerint/<varos>.json')
def search_by_varos(varos):
    """Allomasok varos szerinti keresese - visszadaja azon allomasok listajat, melyek varos attributumara illeszkedik a keresesi feltetel"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # SQL lekerdezes
            # A nevhez hozzaadjuk a "%" karaktert, hogy szo eleji, vegi es kozepi illeszkedesre is talalatot kapjunk
            cur.execute("SELECT id, nev, varos FROM allomas WHERE varos LIKE :var ESCAPE '@'", var="%" + varos + "%")
            # Eredmenytomb
            results = []
            # Feltoltjuk az eredmenytombot
            for id, nev, varos in cur:
                results.append({'id': id, 'nev': nev, 'varos': varos})
            # Ha ures, not found hibaval terunk vissza
            if not results:
                abort(404)
            else:
                return jsonify(allomasok=results)
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/allomasok/nev-szerint/<nev>.json')
def search_by_nev(nev):
    """Allomasok nev szerinti keresese - visszadaja azon allomasok listajat, melyek nevere illeszkedik a keresesi feltetel"""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # SQL lekerdezes
            # A nevhez hozzaadjuk a "%" karaktert, hogy szo eleji, vegi es kozepi illeszkedesre is talalatot kapjunk
            cur.execute("SELECT id, nev, varos FROM allomas WHERE nev LIKE :n ESCAPE '@'", n= "%" + nev + "%")
            # Eredmenytomb
            results = []
            # Feltoltjuk az eredmenytombot
            for id, nev, varos in cur:
                results.append({'id': id, 'nev': nev, 'varos': varos})
            # Ha ures, not found hibaval terunk vissza
            if not results:
                abort(404)
            else:
                return jsonify(allomasok=results)
        finally:
            cur.close()
    finally:
        conn.close()

# 5. feladat megoldasai
@app.route('/allomasok/<id>', methods=['DELETE', 'PUT'])
def delete_or_put(id):
    """Delete es Put verb-eket megvalosito fuggveny. Vegrehajtja a torlest/frissitest a kapott id-ju allomasra."""
    conn = get_db()
    try:
        cur = conn.cursor()
        try:
            # Megnezzuk van e ilyen id-ju allomas az adatbazisban
            cur.execute("SELECT id FROM allomas WHERE id = :id ", id=id)
            result = cur.fetchone()
            # Ha nincs, Not Found hibauzenettel terunk vissza
            if result is None:
                abort(404)
            else:
                # Ha Delete metodusrol van szo toroljuk az allomast az adatbazibol
                if request.method == 'DELETE':
                    try:
                        conn.begin()
                        cur.execute("DELETE FROM allomas WHERE id = :id", id=id)
                        conn.commit()
                        # Az alabbi sor csak azert kellett hogy teszteleskor latszodjon az eredmeny
                        # return jsonify({"deleted": id})
                    except:
                        # Hiba eseten Rollback muveletet hajtunk vegre
                        conn.rollback()
                        abort(500)
                # Ha Put metodusrol van szo frissitjuk az adott id-ju allomas adatait
                elif request.method == 'PUT':
                    # A keres torzseben kapott json formatumu adatok - ezekkel modoitjuk az allomast
                    data = request.json
                    try:
                        conn.begin()
                        cur.execute("UPDATE allomas SET nev = :nev, varos = :varos, atlagutas = :atlagutas, sztrajkutas = :sztrajkutas WHERE id = :id", nev=data['nev'], varos=data['varos'], atlagutas=data['atlagutas'], sztrajkutas=data['sztrajkutas'], id=id)
                        conn.commit()
                        # visszaterunk a modositott allomas id-javal
                        return jsonify({"id": id})
                    except:
                        # Hiba eseten Rollback muveletet hajtnuk vegre
                        conn.rollback()
                        abort(500)  
        finally:
            cur.close()
    finally:
        conn.close()

@app.route('/allomasok.json', methods=['POST'])
def post():
    """Post verb-et megvalosito fuggveny. Beszurja az adatbazisba a keres torzseben kapott allomast"""
    # Ha a methods verb Post
    if request.method == 'POST':
        conn = get_db()
        # elkerjuk a keres torzsebol az adatokat
        data = request.json
        try:
            cur = conn.cursor()
            try:
                conn.begin()
                # Eloszor lekerdezzuk a legnagyobb id-t az adatbazisbol, majd ezt egyel megnovelve mar biztosan egyedi ertekunk lesz
                cur.execute("SELECT MAX(id) as MAX FROM allomas")
                new_id = int(cur.fetchone()[0])
                new_id = new_id + 1
                # Az uj id-val es a kapott adatokkal letrehozzuk az uj allomast
                cur.execute("INSERT INTO allomas VALUES(:id, :nev, :varos, :atlagutas, :sztrajkutas)", id=new_id, nev=data['nev'], varos=data['varos'], atlagutas=data['atlagutas'], sztrajkutas=data['sztrajkutas'])
                conn.commit()
                # Majd visszaterunk az uj allomas id-val json formatumban
                return jsonify({"id": new_id})
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


if __name__ == "__main__":
    # pylint: disable=bad-option-value,wrong-import-position,wrong-import-order
    import os
    os.environ['NLS_LANG'] = '.UTF8'
    app.run(debug=True, port=os.getuid() + 10000)
