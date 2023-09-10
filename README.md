Banka izdaje fizičkim osobama kreditne kartice. Osobe za to apliciraju banci. Za potrebe te evidencije treba napraviti mini-aplikaciju kojom će se evidentirati osoba(O) ili više njih predstavljenih Imenom, Prezimenom, OIB-om i Statusom za koje se treba izraditi kartica.
Osobe se moraju zapisati permanentno.

Kako bi proces za proizvodnju/tiskanje kreditnih kartica znao čiju/koju karticu napraviti, treba mu dati tekstualnu datoteku(D) sa strukturom:
Ime, Prezime, OIB, Status

Aplikacija treba omogućiti:

- Upisivanje osobe(O) u skup osoba sa svim pripadajućim atributima(Ime, Prezime, OIB, Status),
- Pretraživanje skupa osoba(O) prema OIBu(ručni upis korisnika) osobe za koju želimo generirati datoteku(D), i ako osoba(O) postoji, vratiti Ime, Prezime, OIB i Status za istu; Inače ne vrati ništa, a može biti i neki exception da se zna što se desilo.
- Za pronađenu osobu(O) treba napraviti tekstualnu datoteku (D) sa svim popunjenim atributima(Ime, Prezime, OIB, Status).

Jedna datoteka(D) treba sadržavati podatke samo za jednu osobu(O).
Osoba(O) se treba moći obrisati na zahtjev prema OIBu(ručni upis korisnika).

- Jedna osoba(O) može imati samo jednu aktivnu datoteku(D)
- Ako se obriše osoba(O), datoteka treba biti označena kao neaktivna