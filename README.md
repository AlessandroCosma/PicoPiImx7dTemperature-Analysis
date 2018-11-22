# PicoPiImx7dTemperature-Analysis

PicoPiImx7dTemperature è un'applicazione AndroidThings sviluppata per la piattaforma PICO-PI-IMX7-STARTKIT-WITH-RAINBOW-HAT ([link](https://shop.technexion.com/pico-pi-imx7-startkit-rainbow-hat.html) al sito del produttore).

L'app rileva la temperatura dell'ambiente attraverso il sensore Bmx280 presente nel RainbowHat, la mostra all'utente attraverso il display alfanumerico HT16K33 (sempre presente nel RainbowHat) e attiva i led blu, verde o rosso, a seconda se temperatura rilevata è minore di NORMAL_TEMPERATURE, compresa tra NORMAL_TEMPERATURE e MAX_TEMPERATURE oppure maggiore di MAX_TEMPERATURE. 
Solo nel caso in cui il valore soglia MAX_TEMPERATURE venga superato, viene attivato un allarme sonoro attraverso il buzzer pwm Piezo Buzzer.


Valori di default: 

- MAX_TEMPERATURE = 28°C

- NORMAL_TEMPERATURE = 24°C


## Introduzione fase di bootstrap iniziale

Nella seconda versione dell'applicazione [PicoPiImx7dTemperature_v2](https://github.com/AlessandroCosma/PicoPiTemperature_v2) oltre all'introduzione dell Android Architecture Components (AAC), è stata aggiunta una fase iniziale di bootstrap, nella quale viene eseguita una scansione di tutte le componenti collegate al RainbowHat.

Questa fase ha lo scopo di memorizzare le componenti collegate, andando a salvarle in memoria con degli identificativi univoci, ma di facile memorizzazione, rispetto alle stringhe per gli indirizzi, porte e bus che di default servono per identificare un dato componente.




### Classe RainbowHatDictionary e RainbowHatDictManager
