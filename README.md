# PicoPiImx7dTemperature Analysis

PicoPiImx7dTemperature è un'applicazione AndroidThings sviluppata per la piattaforma PICO-PI-IMX7-STARTKIT-WITH-RAINBOW-HAT ([link](https://shop.technexion.com/pico-pi-imx7-startkit-rainbow-hat.html) al sito del produttore).

L'app rileva la temperatura dell'ambiente attraverso il sensore Bmx280 presente nel RainbowHat, la mostra all'utente attraverso il display alfanumerico HT16K33 (sempre presente nel RainbowHat) e attiva i led blu, verde o rosso, a seconda se temperatura rilevata è minore di NORMAL_TEMPERATURE, compresa tra NORMAL_TEMPERATURE e MAX_TEMPERATURE oppure maggiore di MAX_TEMPERATURE. 
Solo nel caso in cui il valore soglia MAX_TEMPERATURE venga superato, viene attivato un allarme sonoro attraverso il buzzer pwm Piezo Buzzer.


Valori di default: 

- MAX_TEMPERATURE = 28°C

- NORMAL_TEMPERATURE = 24°C


## Introduzione fase di bootstrap iniziale

Nella seconda versione dell'applicazione [PicoPiImx7dTemperature_v2](https://github.com/AlessandroCosma/PicoPiTemperature_v2) oltre all'introduzione dell Android Architecture Components (AAC), è stata aggiunta una fase iniziale di bootstrap, nella quale viene eseguita una scansione di tutte le componenti collegate al RainbowHat.

Questa fase ha lo scopo di memorizzare le componenti collegate al RainbowHat, andando a salvarle in memoria. Il salvataggio avviene tramite degli identificativi univoci, ma di facile memorizzazione, rispetto alle stringhe per gli indirizzi, porte e bus che di default servono per identificare un dato componente.

### Implementazione

Attualmente la scansione delle componenti riguarda 3 differenti tipologie: GPIO, PWM, I2C.

##### esempio: executei2cScan()
Prendiamo come esempio la procedura per la scansione delle componenti i2c, presenti nel RainbowHat.
Il metodo che implementa la scansione è executei2cScan(); di seguito il codice:


```java
  private void executei2cScan(){
        String hexAddress;
        String name;
        for (int address = 0; address < 127; address++) {
            //try-with-resources: auto-close the devices
            try (final I2cDevice device = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, address)) {
                try {
                    hexAddress = Integer.toHexString(address);
                    device.readRegByte(0x0);
                    name = RainbowHatDictManager.getDictionaryI2C().get(hexAddress);
                    if (name != null)
                        Log.i("i2cScanner", "Trying: "+hexAddress+" - SUCCESS -> device name = "+name);
                } catch (final IOException e) {
                    //Log.i("i2cScanner", "Trying: "+address+" - FAIL");
                }
            } catch (final IOException e) {
                //in case address not exists, openI2cDevice() generates an exception
            }
        }
    }
 ```
Questo metodo, cicla su tutti i 128 indirizzi disponibili nel bus i2c, e per ognuno di essi:
* apre la connessione verso un dato indirizzo: ``` mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, address)```
* prova a leggere un byte da quelll'indirizzo: ``` device.readRegByte(0x0);```
* se riceve un ACK di ritorno allora vuol dire che il dispositivo connesso al bus I2C e assegnato a quell'indirizzo esiste.
  Altrimenti viene lanciata e gestita un'eccezione, di tipo IOException e il ciclo continua.

**OSSERVAZIONE:** attualmente il codice esegue solo la scanzione e il rilevamento delle componenti. Una volta che esse sono state identificate viene stampato un Log informativo nella console in cui viene specificato il nome del sispositivo rilevato.

### Classe RainbowHatDictionary e RainbowHatDictManager
Un problema non indifferente riguarda l'assegnazione di un identificativo ad un dato componente. Infatti, quando si prova a leggere un byte da un dato indirizzo e riceviamo un ACK di ritorno, come si può sapere quale dispositivo l'ha inviato?
Ovvero: com'è possibile sapere se l'ACK di ritorno proviene da un sensore di temperatura (es: Bmx280) piuttosto che da un sensore di umidità o da un display alfanumerico (es: HT16K33)?

Per risolvere tale problema sono state introdotte le classi RainbowHatDictionary e RainbowHatDictManager.

##### RainbowHatDictionary
La classe RainbowHatDictionary rappresenta un dizionario implementato con HashMap<String, String>.

Costruttori:

* ``` RainbowHatDictionary()``` costruttore che crea un dizionario vuoto.
* ``` RainbowHatDictionary(int initialiCapacity)``` Costruttore che crea un dizionario con una certa capacità iniziale

Metodi:

* ``` put(String key, String name)``` Metodo per inserire un elemento nel dizionario, con chiave _key_ e valore _name_.
* ``` get(String key)``` Metodo per ottenere un valore di tipo Stringa, data la chiave _key_.

##### RainbowHatDictManager
La classe RainbowHatDictManager serve per gestire i dizionari contenenti le associazioni **_indirizzo - id_** dove:
* **_indirizzo_** è il valore (espresso in esadecimale) dell'indirizzo nel bus I2C in cui è collegato uno specifico componente.
* **_id_** è l'identificativo, di facile memorizzazion, scelto per il componente.




