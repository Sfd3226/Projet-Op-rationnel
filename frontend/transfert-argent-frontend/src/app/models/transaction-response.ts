import { Compte } from './compte';

export interface TransactionResponse {
  id: number;
  montant: number;
  frais: number;
  type: 'ENVOI' | 'RECEPTION';
  autrePartie: string;
  nomAutrePartie: string;
  statut: string;
  dateTransaction: Date;
  compteSource: Compte;
  compteDestination: Compte;
}