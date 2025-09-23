export interface Compte {
  id: number;
  solde: number;
  typeCompte: string;
  numeroTelephone: string;
  userId: number;
  user?: { // Optionnel, si disponible
    prenom: string;
    nom: string;
    email: string;
  };
}