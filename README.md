# PicoPiImx7dTemperature Analysis

PicoPiImx7dTemperature è un'applicazione AndroidThings sviluppata per la piattaforma PICO-PI-IMX7-STARTKIT-WITH-RAINBOW-HAT ([link](https://shop.technexion.com/pico-pi-imx7-startkit-rainbow-hat.html) al sito del produttore).

L'app rileva la temperatura dell'ambiente attraverso il sensore Bmx280 presente nel RainbowHat, la mostra all'utente attraverso il display alfanumerico HT16K33 (sempre presente nel RainbowHat) e attiva i led blu, verde o rosso, a seconda se temperatura rilevata è minore di NORMAL_TEMPERATURE, compresa tra NORMAL_TEMPERATURE e MAX_TEMPERATURE oppure maggiore di MAX_TEMPERATURE. 
Solo nel caso in cui il valore soglia MAX_TEMPERATURE venga superato, viene attivato un allarme sonoro attraverso il buzzer pwm Piezo Buzzer.


Valori di default: 

- MAX_TEMPERATURE = 28°C

- NORMAL_TEMPERATURE = 24°C


## 1) Introduzione di una fase di bootstrap iniziale
Nella seconda versione dell'applicazione [PicoPiImx7dTemperature_v2](https://github.com/AlessandroCosma/PicoPiTemperature_v2) oltre all'introduzione dell Android Architecture Components (AAC), è stata aggiunta una fase iniziale di bootstrap, nella quale viene eseguita una scansione di tutte le componenti collegate al RainbowHat.

Questa fase ha lo scopo di memorizzare le componenti collegate al RainbowHat, andando a salvarle in memoria. Il salvataggio avviene tramite degli identificativi univoci, ma di facile memorizzazione, rispetto alle stringhe per gli indirizzi, porte e bus che di default servono per identificare un dato componente.

### Implementazione

Attualmente la scansione delle componenti riguarda 3 differenti tipologie: GPIO, PWM, I2C.

##### esempio: executei2cScan()
Prendiamo come esempio la procedura per la scansione delle componenti i2c presenti nel RainbowHat.
Il metodo che implementa la scansione è executei2cScan(); di seguito il codice:


```java
private void executei2cScan(){
  String hexAddress;
  String name;
  for (int address = 0; address <= 127; address++) {
    //try-with-resources: auto-close the devices
    try (final I2cDevice device = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, address)) {
      try {
        hexAddress = Integer.toHexString(address);
        device.readRegByte(0x0);
        name = RainbowHatDictManager.getDictionaryI2C().get(hexAddress);
        if (name != null)
            Log.i("i2cScanner", "Trying: "+hexAddress+" - SUCCESS -> device name = "+name);
      } catch (final IOException e) {
            Log.i("i2cScanner", "Trying: "+address+" - FAIL");
      }
    } catch (final IOException e) {
        // In case address not exists, openI2cDevice() generates an exception
        Log.e(TAG, "address "+address +" doesn't exist!");
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
Ovvero: com'è possibile sapere se l'ACK di ritorno proviene da un sensore di temperatura (es: Bmp280) piuttosto che da un sensore di umidità o da un display alfanumerico (es: HT16K33)?

Per risolvere tale problema sono state introdotte le classi RainbowHatDictionary e RainbowHatDictManager.

#### RainbowHatDictionary
  La classe RainbowHatDictionary rappresenta un dizionario implementato con HashMap<String, String>.

  Costruttori:

  * ``` RainbowHatDictionary()``` costruttore che crea un dizionario vuoto.
  * ``` RainbowHatDictionary(int initialiCapacity)``` Costruttore che crea un dizionario con una certa capacità iniziale

  Metodi:

  * ``` put(String key, String name)``` Metodo per inserire un elemento nel dizionario, con chiave _key_ e valore _name_.
  * ``` get(String key)``` Metodo per ottenere un valore di tipo Stringa, data la chiave _key_.


#### RainbowHatDictManager
  La classe RainbowHatDictManager serve per gestire i dizionari contenenti le associazioni **_indirizzo - id_** dove:
  * **_indirizzo_** è il valore dell'indirizzo (I2C) o della porta (GPIO o PWM) a cui è collegato uno specifico componente.
  * **_id_** è l'identificativo univoco, di facile memorizzazion, scelto per il componente.


Attraverso queste due classi, è possibile implementare e popolare dizionari contenenti l'assoziazione fra le porte/indirizzi che identificano uno specifico componente nel RainbowHat e gli identificativi scelti.

##### esempio: dictionaryI2C
```dictionaryI2C``` è un dizionario che raccoglie le associazioni tra gli indirizzi esadecimali, assegnati di default, di un dato componente I2C e il nome univoco scelto per quel componente.

Il RainbowHat ha installato 2 dispositivi i2C:
* **Bmp280** sensore di pressione e temperatura, a cui è assegnato di default l'indirizzo slave 0x77.
* **HT16K33** display alfanumerico a 14 segmenti, a cui è assegnato di default l'indirizzo slave 0x70.

Il dizionario dictionaryI2C viene implementato nel modo seguente:
```java
private static RainbowHatDictionary dictionaryI2C = new RainbowHatDictionary(2){
    {
        put(Integer.toHexString(0x77), "TEMP_SENSOR");
        put(Integer.toHexString(0x70), "SEGMENT_DISPALY");
    }
};
```
Si può notare come il dizionario associ al sensore di temperatura, posizionato all'indirizzo 0x77 del bus I2C, l'identificativo **_TEMP_SENSOR_**, e al display alfanumerico a 14 segmenti, posizionato all'indirizzo 0x70 del bus I2C, l'identificativo **_SEGMENT_DIPLAY_**.

All'interno di questo dizionario gli identificativi scelti sono univochi.


**OSSERVAZIONE**: avendo un numero limitato di dispositivi I2C che possono essere connessi come slave al bus I2C di RainbowHat (al più 128), i nomi scelti come identificativi possono ben rispecchiare il dispositivo che vanno ad identificare. 


### Annotazioni per Julia Analyzer
L'introduzione di questa fase iniziale di bootstrap e quindi l'identificazione delle componenti tramite nomi univoci di facile memorizzazione, ha come scopo principale il supporto alle annotazioni per l'analizzatore Julia.

Nel momento in cui si apre la connessione con un dato componente, si possono utilizzare questi nomi all'interno delle annotazioni ```@inputComponent``` e ```@outputComponent```

Per esempio nel caso si voglia aprire una connessione con il display alfanumerico HT16K33, il cui identificativo all'interno del dizionario dictionaryI2C è SEGMENT_DISPLAY, il programmatore può annotare il codice conm la seguente annotazione:
```java
@OutputComponent(type = "I2C", name = "SEGMENT_DISPLAY")
public static AlphanumericDisplay openDisplay() throws IOException {
	return new AlphanumericDisplay(BOARD.getI2cBus());
}
```









