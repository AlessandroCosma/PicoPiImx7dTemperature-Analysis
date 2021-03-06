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

Nel momento in cui si apre la connessione con un dato componente, si possono utilizzare questi nomi all'interno delle annotazioni (**da implementare**) ```@inputComponent``` e ```@outputComponent```

Per esempio nel caso si voglia aprire una connessione con il display alfanumerico HT16K33, il cui identificativo all'interno del dizionario dictionaryI2C è SEGMENT_DISPLAY, il programmatore può annotare il codice con la seguente annotazione:
```java
@OutputComponent(type = "I2C", name = "SEGMENT_DISPLAY")
public static AlphanumericDisplay openDisplay() throws IOException {
	return new AlphanumericDisplay(BOARD.getI2cBus());
}
```
Analogamente, nel caso di apertura di una connessione con il led rosso, si avrà la seguente annotazione:
```java
@OutputComponent(type = "GPIO", name = "RED_LED")
public static Gpio openLedRed()
```
Infatti nella classe [RainbowHatDictManager](https://github.com/AlessandroCosma/PicoPiImx7dTemperature_v2/blob/master/app/src/main/java/com/alessandrocosma/picopiimx7dtemperature/RainbowHatDictManager.java) il dizionario ```dictionaryGPIO``` associa all'identificativo per il led rosso, che è GPIO2_IO02, il nome RED_LED.

Quindi nel momento in cui è presente l'associzione ```@OutputComponent(type = "GPIO", name = "RED_LED") ``` l'analizzatore sa che viene aperta una connessione con un componente GPIO, il cui nome è RED_LED e la cui porta è GPIO2_IO02.


## 2) Android Architecture Components e CloseResource Checker

Per osservare con più attenzione il comportamento del checker CloseResource di Julia, nel caso dell'utilizzo degli Android Architecture Components, si sono sviluppate 2 applicazioni di testing, che riproducono in parte il comportamento dell'appliczione PicoPiTemperature_v2.

In PicoPiTemperature_v2, nelle classi LiveData, l'apertura delle connessioni con le componenti del PicoPi (oggetti di tipo Closeable) avviene nel metodo onActive(), mentre la loro chiusura avviene nel metodo onInactive().

Con le applicazioni di testing si vuole quindi studiare il comportamento del checker, quando l'apertura/chiusura di connessioni con questi oggetti Closeable avviene in due metodi separati.

### Applicazione PicoPiComponentTest

L'applicazione è composta da una singola Activity e 2 metodi: onCreate e onDestroy.

L'applicazione si concentra sull'utilizzo (apertura e chiusura) della variabile mI2CDevice che è un oggetto I2CDevice.

mI2CDevice viene dichiarato globale: 

```java
private I2cDevice mI2cDevice;
```

Nel metodo onCreate vado ad aprire una connessione: 

```java
try{
    PeripheralManager mPeripheralManager = PeripheralManager.getInstance();
    mI2cDevice = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
}
catch(IOException e){...}
```
	
Nel metodo onDestroy vado a chiudere la connesione:

```java
try {
    mI2cDevice.close();
} catch (IOException e) {...}
```

L'analizzatore Julia dà il seguente warning:
```
warningDescriptio:	a closeable has not been immediately stored into a local variable
warningMessage:		Instances of class "I2cDevice" should be immediately stored into a local variable, for later being closed
```

Cosa possiamo dire di questo warning?
* Julia non si accorge che viene chiuso nel metodo onDestroy?
* Julia se ne accorge ma ritiene che sia più corretto salvare mI2CDevice in locale al metodo e chiuderlo li? (quindi un falso allarme)


Nel caso in cui salvo mI2CDevice in locale al metodo onCreate, poi non posso fare la seguente chiamata nel metodo onDestroy:

```java
@Override
protected void onDestroy() {
    ...
	
    try {
       	mI2cDevice.close();
    } catch (IOException e) {...}
            
    ...
}
```
    
perchè la variabile mI2CDevice non rientra nello scope del metodo onDestroy.

Nel caso di dispositivi di output, l'idea sarebbe quella di aprire la connesione verso una determinata componente _X_ nel metodo onCreate(), assegnadno tale istanza alla variabile _x1_; settare il valore per la componente X, chiamando il metodo x1.setValue(); successivamente chiuderle la connessione.

Prima dell'uscita dall'applicazione, nel metodo onDestroy, riaprire la connessione con la componente _X_ assegnando la nuova istanza alla variabile x2, settare il valore che si vuole attribuire a _X_ all'uscita dell'applicazione e chiudere nuovamente la connesione.

Questo procedura è limitata ad alcune componenti di output, ad esempio un led o un allarme attivo, i quali una volta settato un certo valore (es: setValue(true)) lo mantengono fino a quando non viene nuovamente settato il un valore (setValue(false)).

Per dispositivi di output come sensori di temperatura o pressione, i cui valori rilevati hanno necessità di essere letti in modo continuo, estendere questa procedura non sarebbe fattibile.

In questo caso è essenziale separare i 2 concetti: apertura e chiusura di una connessione rispetto all'attivazione/disattivazione della funzionalità del componente connesso.


### Applicazione PicoPiComponentTest2

L'applicazione è composta da una singola Activity e 2 metodi: onCreate e onDestroy.
L'applicazione si concentra sull'utilizzo (apertura e chiusura) della variabile mI2CDevice che è un oggetto I2CDevice (cioè un Closeble).

mI2cDevice viene dichiarata e inizializzata all'interno del metodo onCreate.

```java
try {
    PeripheralManager mPeripheralManager = PeripheralManager.getInstance();
    mI2cDevice = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
} catch (IOException e){
    Log.e(TAG, "Unable to access I2C device", e);
}

...

if (mI2cDevice != null) {
    try {
        mI2cDevice.close();
    } catch (IOException e) {
        Log.w(TAG, "Unable to close I2C device", e);
    }
}
```

L'apertura e la chiusura della connessione con l'oggetto I2CDevice, sono interne al metodo onCreate e Julia non emette alcun warning.


Se si prova invece a chiudere la risorsa con un metodo dedicato ```myCloseMethod(mI2cDevice);```, che a sua volta chiama il metodo close():
```java
private void myCloseMethod(I2cDevice mDevice){
    if (mDevice != null) {
        try {
            mDevice.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close I2C device", e);
        }
    }
}
```
osserviamo che l'analizzatore emette il warning seguente:
```
warningDescription:	a resource should be closed by the end of the method where it is created
warningMessage		This instance of class "I2cDevice" does not seem to be always closed by the end of this method. It seems leaked if an exception occurs at line 47 before being closed
```

