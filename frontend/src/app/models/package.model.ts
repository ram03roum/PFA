export interface Package {
  id: number;
  title: string;
  price: number;
  image: string;
  duration: string;
  accommodation: string;
  transportation: boolean;
  food: boolean;
  rating: number;
  reviews: number;
}

export interface Destination {
  id: number;
  name: string;
  image: string;
  tours: number;
  places: number;
}

export interface Testimonial {
  id: number;
  name: string;
  location: string;
  image: string;
  text: string;
}

export interface BlogPost {
  id: number;
  title: string;
  date: string;
  image: string;
  excerpt: string;
}

export interface Service {
  id: number;
  icon: string;
  title: string;
  description: string;
}